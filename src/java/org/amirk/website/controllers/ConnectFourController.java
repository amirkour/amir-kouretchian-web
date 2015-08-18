package org.amirk.website.controllers;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

@Controller
@RequestMapping(value="/connectfour")
public class ConnectFourController extends BaseController {
    
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
    public String showGame(@PathVariable("gameId") long gameId){
        return "connectfour/play";
    }
    
    @RequestMapping(method = RequestMethod.POST, value="/play/{gameId}")
    public String makeMoveForGame(){
        return "foo";
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
        if(game == null || nextPlayerToMove == null){ throw new Exception("Cannot render game board for missing or invalid game and/or player objects - please report this error!"); }
        
        long[][] board = game.getBoardMatrix();
        if(board == null){ throw new Exception("Cannot render game board for missing or invalid board matrix - please report this error!"); }
        
        // as we render the board, we'll give the next player a button to push
        // if we encounter a legal move (and assuming the next player to move
        // is human.)
        SortedSet<GameMove> legalMoves = game.getLegalMoves();
        
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<table>");
        
        // the html table will render from top-to-bottom, left-to-right, so
        // we have to flip-flop the row and column indices to get the layout right:
        for(int j = 0; j < board[0].length; j++){
            htmlBuilder.append("<tr>");
            for(int i = 0; i < board.length; i++){
                
                // if the next player is human and this i,j spot is a legal move,
                // give the player a form/button they can use at this spot to make
                // a move
                if(nextPlayerToMove.isConsideredHuman() && legalMoves.contains(new GameMove(i,j))){
                
                    UriComponents uriBuilder = ServletUriComponentsBuilder.fromContextPath(request)
                                                                          .path("/connectfour/play/" + game.getId())
                                                                          .build();
                    htmlBuilder.append("<td>");
                    htmlBuilder.append("<form method=\"POST\" action=\"" + uriBuilder.encode().toString() + "\" >");
                    htmlBuilder.append("<input type=\"hidden\" name=\"row\" value=\"" + i + "\" />");
                    htmlBuilder.append("<input type=\"hidden\" name=\"col\" value=\"" + j + "\" />");
                    htmlBuilder.append("<input type=\"hidden\" name=\"playerId\" value=\"" + nextPlayerToMove.getId() + "\" />");
                    htmlBuilder.append("<input type=\"submit\" value=\"X\" />");
                    htmlBuilder.append("</form>");
                    htmlBuilder.append("</td>");
                    
                // otherwise, just render this i,j spot normally. it's either a blank spot,
                // or occupied by somebody who played here previously
                }else{
                
                    String colorOfThisSpot = game.getColorForPlayerAt(i, j);
                    if(StringUtils.isBlank(colorOfThisSpot)){
                        htmlBuilder.append("<td>");
                    }else{
                        htmlBuilder.append("<td style=\"background-color: " + colorOfThisSpot + ";\" >");
                    }
                    
                    htmlBuilder.append("</td>");
                }
            }
            htmlBuilder.append("</tr>");
        }
        htmlBuilder.append("</table>");
        
        return htmlBuilder.toString();
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
