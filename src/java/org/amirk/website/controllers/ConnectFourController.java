package org.amirk.website.controllers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.amirk.games.connectfour.entities.*;
import org.amirk.games.connectfour.db.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.amirk.games.connectfour.agents.*;
import org.springframework.context.ApplicationContext;

@Controller
@RequestMapping(value="/connectfour")
public class ConnectFourController extends BaseController {
    
    public final static String MINIMAX_AGENT_OFFENSIVE = "npc-offensive-minimax-agent";
    public final static String MINIMAX_AGENT_DEFENSIVE = "npc-defensive-minimax-agent";
    
    @Autowired
    protected ApplicationContext appContext;
    
    @Autowired
    protected DAOPlayerType daoPlayerType;
    
    @Autowired
    protected DAOUser daoUser;
    
    @Autowired
    protected DAOPlayer daoPlayer;
    
    @Autowired
    protected DAOPlayerColor daoPlayerColor;
    
    @Autowired
    protected DAOGame daoGame;
    
    @RequestMapping(method = RequestMethod.GET)
    public String index(){ return "connectfour/index"; }
    
    @RequestMapping(method=RequestMethod.POST, value="/play")
    public String playNewGame(@RequestParam("boardWidth") int boardWidth,
                              @RequestParam("boardHeight") int boardHeight,
                              @RequestParam("numberInRowToWin") short numberInRowToWin,
                              @RequestParam("playerOneType") String strPlayerOneType,
                              @RequestParam("playerTwoType") String strPlayerTwoType,
                              RedirectAttributes flash){
        if(numberInRowToWin <= 0){ return this.flashInfoAndRedirect("/connectfour", "Received bad number-in-a-row value - must be a positive integer", flash); }
        
        PlayerType playerOneType = this.daoPlayerType.getByName(strPlayerOneType);
        if(playerOneType == null){ return this.flashErrorAndRedirect("/connectfour", "Could not find a player type for " + strPlayerOneType, flash); }
        
        PlayerType playerTwoType = this.daoPlayerType.getByName(strPlayerTwoType);
        if(playerTwoType == null){ return this.flashErrorAndRedirect("/connectfour", "Could not find a player type for " + strPlayerTwoType, flash); }
        
        long[][] newGameBoard = new long[boardWidth][boardHeight];
        List<Player> gamePlayers = null;
        try{
            gamePlayers = this.getTestPlayersWithTypes(playerOneType, playerTwoType);
        }catch(Exception e){
            return this.flashErrorAndRedirect("/connectfour", "Encountered the following error while attempting to create new game players (please report this error): " + e.getMessage(), flash);
        }
        
        if(gamePlayers == null || gamePlayers.size() != 2){ return this.flashErrorAndRedirect("/connectfour", "Failed to retrieve test players for new game - please report this error!", flash); }
        
        Game newGame = new Game();
        newGame.setNumberInRowToWin(numberInRowToWin);
        newGame.setPlayers(gamePlayers);
        newGame.setBoardMatrix(newGameBoard);
        this.daoGame.save(newGame);
        
        return this.redirect("/connectfour/play/" + newGame.getId());
    }
    
    @RequestMapping(method=RequestMethod.GET, value="/play/{gameId}")
    public String showGame(@PathVariable("gameId") long gameId, 
                           Model model,
                           RedirectAttributes flash, 
                           HttpServletRequest request){
        
        Game game = this.daoGame.getById(gameId);
        if(game == null){ return this.flashErrorAndRedirect("/connectfour", "Could not retrieve game with id " + gameId, flash); }
        
        Player nextPlayerToMove = null;
        try{
            nextPlayerToMove = this.getPlayerWhosTurnItIsFor(game);
        }catch(Exception e){
            return this.flashErrorAndRedirect("/connectfour", "Failed with the following error: " + e.getMessage(), flash);
        }
        
        String boardHtmlString = null;
        try{
            boardHtmlString = this.getBoardHtmlFor(game, nextPlayerToMove, request);
        }catch(Exception e){
            return this.flashErrorAndRedirect("/connectfour", "Failed with the following error: " + e.getMessage(), flash);
        }
        
        model.addAttribute("game", game);
        model.addAttribute("boardHtml", boardHtmlString);
        model.addAttribute("nextPlayerToMove", nextPlayerToMove);
        
        // if one player is human, display some helpful instructions on the page
        if(!this.allPlayersAreAIFor(game)){ model.addAttribute("showInstructions", true); }
        
        // if the next player to move is non-null and is not a human, the game
        // should automatically follow-up with the AI's move.  let the view know
        // whether or not that's the case, so we can work some magic to perform
        // an auto-post with javascript.
        if(nextPlayerToMove != null && !nextPlayerToMove.isConsideredHuman()){ model.addAttribute("autoPost", true); }
        
        return "connectfour/play";
    }
    
    @RequestMapping(method = RequestMethod.POST, value="/play/{gameId}")
    public String makeMoveForGame(@PathVariable("gameId") long gameId,
                                  @RequestParam("row") int row,
                                  @RequestParam("col") int col,
                                  @RequestParam("playerId") long playerId,
                                  RedirectAttributes flash){
        
        String errorRedirectUrl = "/connectfour";
        
        Game game = this.daoGame.getById(gameId);
        if(game == null){ return this.flashErrorAndRedirect(errorRedirectUrl, "Could not find game with id " + gameId, flash); }
        
        // if the game is already over, just redirect with some feedback
        if(game.outcomeAlreadyDetermined()){ return this.flashInfoAndRedirect("/connectfour/play/" + gameId, "Game " + gameId + " is already finished!  See below for info", flash); }
        
        Player thisPlayer = game.getPlayerWithId(playerId);
        if(thisPlayer == null){ return this.flashErrorAndRedirect(errorRedirectUrl, "Player " + playerId + " does not exist for game " + gameId, flash); }
        
        ConnectFourGameAgent agent = this.getAgentFor(thisPlayer.getPlayerType(), row, col);
        if(agent == null){ return this.flashErrorAndRedirect(errorRedirectUrl, "Failed to map player " + playerId + " to a game agent", flash); }
        
        // delegate to the agent for this player's next move, then apply that move to the game
        // and then immediately check if any game-end conditions are met.
        try{
            GameMove thisPlayersMove = agent.getMoveFor(game, thisPlayer);
            game.occupySpot(thisPlayer, thisPlayersMove.getRow(), thisPlayersMove.getCol());
            game.isGameOver();
            this.daoGame.update(game);
        }catch(Exception e){
            return this.flashErrorAndRedirect(errorRedirectUrl, "Failed to apply this players game move with the following error: " + e.getMessage(), flash);
        }
        
        // if this player's move ended the game, kick the result out
        if(game.outcomeAlreadyDetermined()){ return this.redirect("/connectfour/play/" + gameId); }
        
        // now get the next player for this game - depending on what type of player
        // it is, we have some decisions to make ...
        Player nextPlayerToMove = this.getOtherPlayer(game, thisPlayer);
        if(nextPlayerToMove == null){ return this.flashErrorAndRedirect(errorRedirectUrl, "Failed to retrieve other player from game " + gameId, flash); }
        
        // if the player that just moved is human, and the next player is an AI,
        // we should calculate the AI's move in response to the human's move, so
        // that when they next see the game board, they'll see the AIs response
        // and can continue the game immediately.
        //
        // there's one exception: if the next player is AI, but 
        if(!nextPlayerToMove.isConsideredHuman() &&
           thisPlayer.isConsideredHuman()){
            agent = this.getAgentFor(nextPlayerToMove.getPlayerType(), row, col);
            if(agent == null){ return this.flashErrorAndRedirect(errorRedirectUrl, "Failed to map next player " + nextPlayerToMove.getId() + " to a game agent", flash); }
        
            try{
                GameMove nextPlayersMove = agent.getMoveFor(game, nextPlayerToMove);
                game.occupySpot(nextPlayerToMove, nextPlayersMove.getRow(), nextPlayersMove.getCol());
                game.isGameOver();
                this.daoGame.update(game);
            }catch(Exception e){
                return this.flashErrorAndRedirect(errorRedirectUrl, "Failed to apply next players game move with the following error: " + e.getMessage(), flash);
            }
        }
        
        return this.redirect("/connectfour/play/" + gameId);
    }
    
    /*
     * Helper that returns true if all the players in the given game
     * are AI, false otherwise.
     */
    protected Boolean allPlayersAreAIFor(Game game){
        if(game == null){ return false; }
        
        List<Player> players = game.getPlayers();
        if(players == null){ return false; }
        
        for(Player p : players){
            if(p.isConsideredHuman()){ return false; }
        }
        
        return true;
    }
    
    /*
     * Helper that returns the player not equal to that given by the caller
     * from the given game, or null if no such player exists.
     */
    protected Player getOtherPlayer(Game game, Player thisPlayer){
        if(game == null || thisPlayer == null){ return null; }
        
        List<Player> players = game.getPlayers();
        if(players == null || players.size() != 2){ return null; }
        
        for(Player p : players){
            if(p.getId() != thisPlayer.getId()){ return p; }
        }
        
        return null;
    }
    
    /*
     * Factory method for returning a connect four game agent given
     * a player type, and a row/col tuple on the board.
     */
    protected ConnectFourGameAgent getAgentFor(PlayerType playerType, int row, int col){
        if(playerType == null){ return null; }
        
        String typeName = playerType.getName();
        if(StringUtils.isBlank(typeName)){ return null; }
        
        ConnectFourGameAgent agent = null;
        switch(typeName){
            case "pc":
                agent = new PassThroughAgent(row, col);
                break;
            case "npc-left-to-right-agent":
                agent = new NPCLeftToRightDummyAgent();
                break;
            case ConnectFourController.MINIMAX_AGENT_DEFENSIVE:
                agent = this.getDefensiveMinimaxAgent();
                break;
            case ConnectFourController.MINIMAX_AGENT_OFFENSIVE:
                agent = this.getOffensiveMinimaxAgent();
                break;
            
            default:
                break; // todo - log something here
        }
        
        return agent;
    }
    
    /*
     * Helper that constructs/returns an offensive minimax connect four
     * game agent.
     */
    protected ConnectFourGameAgent getOffensiveMinimaxAgent(){
        NPCConfigurableAgent agent = new NPCConfigurableAgent(
                100,  // longest sequence coefficient
                1000, // winning sequence coefficient
                30,  // adjacent spot coefficient
                10,  // opponent's longest sequence coefficient
                3000, // opponent's winning sequence coefficient
                10,  // opponent's adjacent spot coefficient
                2);  // depth limit
        
        return agent;
    }
    
    /*
     * Helper that constructs/returns a defensive minimax connect four
     * game agent.
     */
    protected ConnectFourGameAgent getDefensiveMinimaxAgent(){
        NPCConfigurableAgent agent = new NPCConfigurableAgent(
                10,  // longest sequence coefficient
                1000, // winning sequence coefficient
                10,  // adjacent spot coefficient
                50,  // opponent's longest sequence coefficient
                3000, // opponent's winning sequence coefficient
                40,  // opponent's adjacent spot coefficient
                2);  // depth limit
        
        return agent;
    }
    
    /*
     * Helper that constructs the html string for the game board, replete
     * with interactable buttons and all (where appropriate.)
     *
     * Constructing the equivlent html in the jsp would be ... ugly.
     * Not that this is necessarily beautiful, but I think it's preferable
     * to all the script in a jsp.
     */
    protected String getBoardHtmlFor(Game game, Player nextPlayerToMove, HttpServletRequest request) throws Exception {
        if(game == null){ throw new Exception("Cannot render game board for missing game - please report this error!"); }
        
        long[][] board = game.getBoardMatrix();
        if(board == null){ throw new Exception("Cannot render game board for missing or invalid board matrix - please report this error!"); }
        
        // as we render the board, we'll give the next player a button to push
        // if we encounter a legal move (and assuming the next player to move
        // is human.)
        SortedSet<GameMove> legalMoves = game.getLegalMoves();
        
        // then again, if the game is already over, there shouldn't be any
        // available inputs for the user
        Boolean gameIsOver = game.outcomeAlreadyDetermined();
        
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<table class=\"board\" >");
        
        // the html table will render from top-to-bottom, left-to-right, so
        // we have to flip-flop the row and column indices to get the layout right:
        for(int j = 0; j < board[0].length; j++){
            htmlBuilder.append("<tr>");
            for(int i = 0; i < board.length; i++){
                htmlBuilder.append("<td>");
                
                // if the next player is human and this i,j spot is a legal move,
                // give the player a form/button they can use at this spot to make
                // a move (if the game isn't over!)
                if(!gameIsOver &&
                   nextPlayerToMove != null &&
                   nextPlayerToMove.isConsideredHuman() && 
                   legalMoves != null &&
                   legalMoves.contains(new GameMove(i,j))){
                
                    UriComponents uriBuilder = ServletUriComponentsBuilder.fromContextPath(request)
                                                                          .path("/connectfour/play/" + game.getId())
                                                                          .build();
                    
                    htmlBuilder.append("<div class=\"board-square\" >");
                    htmlBuilder.append("<form method=\"POST\" action=\"" + uriBuilder.encode().toString() + "\" >");
                    htmlBuilder.append("<input type=\"hidden\" name=\"row\" value=\"" + i + "\" />");
                    htmlBuilder.append("<input type=\"hidden\" name=\"col\" value=\"" + j + "\" />");
                    htmlBuilder.append("<input type=\"hidden\" name=\"playerId\" value=\"" + nextPlayerToMove.getId() + "\" />");
                    htmlBuilder.append("<input type=\"submit\" value=\"X\" />");
                    htmlBuilder.append("</form></div>");
                    
                // otherwise, just render this i,j spot normally. it's either a blank spot,
                // or occupied by somebody who played here previously
                }else{
                
                    String colorOfThisSpot = game.getColorForPlayerAt(i, j);
                    if(StringUtils.isBlank(colorOfThisSpot)){
                        htmlBuilder.append("<div class=\"board-square\" />");
                    }else{
                        htmlBuilder.append("<div class=\"board-square\" style=\"background-color: " + colorOfThisSpot + ";\" />");
                    }
                }
                
                htmlBuilder.append("</td>");
            }
            htmlBuilder.append("</tr>");
        }
        htmlBuilder.append("</table>");
        
        return htmlBuilder.toString();
    }
    
    /*
     * Helper that will determine who's turn it is in the given game, and will
     * return that player.  If nobody has played in the given game yet, then
     * this helper will return the first human player from the game's list of
     * players.  If no human
     * players are in the game, then this helper just returns the first
     * player in the game's list.
     */
    public Player getPlayerWhosTurnItIsFor(Game game) throws Exception{

        if(game == null){ throw new Exception("Cannot get next player without a game object"); }
        
        long[][] board = game.getBoardMatrix();
        if(board == null){ throw new Exception("Cannot get next player for null board"); }

        List<Player> players = game.getPlayers();
        if(players == null || players.size() != 2){ throw new Exception("Cannot get next player for game without exactly 2 players"); }

        // count the number of times both players have put a piece on the board
        // (by mapping player IDs to their number of pieces.)
        Map<Long,Integer> map = new HashMap<Long,Integer>();
        for(Player p : players){ map.put(p.getId(), 0); }

        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[0].length; j++){
                if(board[i][j] > 0){
                    if(map.containsKey(board[i][j])){
                        map.put(board[i][j], map.get(board[i][j]) + 1);
                    }else{
                        map.put(board[i][j], 1);
                    }
                }
            }
        }

        // whichever player has fewer pieces on the board gets to go next
        int smallestValue = 0;
        long idOfSmallest = 0;
        for(Map.Entry<Long,Integer> entry : map.entrySet()){
            long nextId = entry.getKey();
            int nextValue = entry.getValue();
            if(nextId != 0){
                if(idOfSmallest == 0){
                    idOfSmallest = nextId;
                    smallestValue = nextValue;
                }else if(nextValue < smallestValue){
                    idOfSmallest = nextId;
                    smallestValue = nextValue;
                }
            }
        }
        
        // if we found an ID for the next player, kick them back ...
        if(idOfSmallest > 0){ return game.getPlayerWithId(idOfSmallest); }

        // ... otherwise, this game hasn't been played at all yet.  if there's a human
        // player, we'll return the first one
        for(Player p : players){
            if(p.isConsideredHuman()){ return p; }
        }
        
        // at this point, nobody has made a move in this game, and there are no
        // human players. just give back the first player in the game.
        return players.get(0);
    }
    
    /*
     * Helper that will try and fetch/return a user from the database with the given
     * email address.  If such a user is not found in the database, one is created
     * with phony/random first and last names.
     */
    protected User getOrCreateUserFor(String email){
        if(StringUtils.isBlank(email)){ return null; }
        
        User user = this.daoUser.getByEmail(email);
        if(user == null){
            int lengthOfRandomNames = 5; // totally arbitrary/magic number
            user = new User();
            user.setEmail(email);
            user.setFirstName(RandomStringUtils.random(lengthOfRandomNames,true,true));
            user.setLastName(RandomStringUtils.random(lengthOfRandomNames,true,true));
            this.daoUser.save(user);
        }
        
        return user;
    }
    
    /*
     * Helper that will return a list of test player objects for a new connect-4 game.
     * TODO - for now, it's just creating new players in the database every time.
     *        it should pull pre-populated test players instead.
     */
    protected List<Player> getTestPlayersWithTypes(PlayerType playerOneType, PlayerType playerTwoType) throws Exception{
        if(playerOneType == null || playerTwoType == null){ return null; }
        
        String testUserOneEmail = "test.one@phony.com";
        String testUserTwoEmail = "test.two@phony.com";
        
        User userOne = this.getOrCreateUserFor(testUserOneEmail);
        if(userOne == null){ throw new Exception("Failed to get a user for test user email " + testUserOneEmail + " - please report this error!"); }
        
        User userTwo = this.getOrCreateUserFor(testUserTwoEmail);
        if(userTwo == null){ throw new Exception("Failed to get a user for test user email " + testUserTwoEmail + " - please report this error!"); }
        
        List<PlayerColor> availablePlayerColors = this.daoPlayerColor.getList();
        if(availablePlayerColors == null || availablePlayerColors.size() != 2){ throw new Exception("Failed to retrieve a proper list of player colors from the database - please report this error!"); }
        
        List<Player> playersToReturn = new ArrayList<Player>();
        
        Player playerOne = new Player();
        playerOne.setPlayerColor(availablePlayerColors.get(0));
        playerOne.setUser(userOne);
        playerOne.setPlayerType(playerOneType);
        this.daoPlayer.save(playerOne);
        playersToReturn.add(playerOne);
        
        Player playerTwo = new Player();
        playerTwo.setPlayerColor(availablePlayerColors.get(1));
        playerTwo.setUser(userTwo);
        playerTwo.setPlayerType(playerTwoType);
        this.daoPlayer.save(playerTwo);
        playersToReturn.add(playerTwo);
        
        return playersToReturn;
    }
}
