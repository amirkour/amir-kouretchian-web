<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="container">
    <div style="margin-bottom:20px; font-size: 0.8em;">
        <a href="<c:url value="/" />">back to homepage</a>
    </div>
    <h2>Connect Four Game Agents</h2>
    
    <div class="body">
        <h3>Project Introduction</h3>
        <hr/>
        <p>
            In the summer of 2015, I decided
            to take a step away from my 9-to-5 to give myself a chance to contemplate
            and pursue the next step in my career.
        </p>
        <p>
            I decided to make a foray into artificial intelligence (at the awesome 
            curriculum at <a href="https://www.cs.washington.edu/">the University of Washington's CSE department</a>)
            and (long-story-short) Connect 4 became a forum for me to begin playing
            with implementations of some introductory AI algorithms.  I had also
            decided to revisit my "native" programming language (Java) which I had
            been away from since graduating from UW in 2006, and which I had been
            wanting to get back to (for personal/subjective reasons.)
        </p>
        
        <h3>Project Details</h3>
        <hr/>
        <p>
            Currently, I've implemented 2 agents:
            <ul class='bulleted-list'>
                <li>a configurable depth-limited Minimax Agent</li>
                <li>a dummy/test agent</li>
            </ul>
            <p>
            Use the section below to configure/play against one of these agents, or to watch them
            battle it out against one-another (which is surprisingly satisfying.)
            </p>
            <p>
            For agent implementation details, see my comments below.
            </p>
        </p>
        
        <h3>Agent Details</h3>
        <hr/>
        <p>
            <ul class='bulleted-list'>
                <li>
                    <p>
                        <em>Minimax</em>: this is a depth-limited implementation of a classic
                        adversarial search algorithm.  Below, you can configure the number of plies
                        that the game agent will search (where each ply is a move by both players),
                        along with the utility value of various aspects of the game-state.  There
                        are also pre-configured agents for convenience: one highly offensive, and one
                        highly defensive.
                    </p>
                    <p>
                    See the <a href="https://en.wikipedia.org/wiki/Minimax">Wikipedia article</a> for a formal explanation of minimax.
                    </p>
                    <p>
                    My implementation was derived based on Stuart Russell and Peter Norvig's 
                    description in <a href="http://www.amazon.com/Artificial-Intelligence-Modern-Approach-Edition/dp/0136042597">
                        Artificial Intelligence, A Modern Approach (3rd Edition)</a>.
                    </p>
                </li>
                <li>
                    <p>
                        <em>Dummy/test agent</em>: pretty brain-dead - the test agent simply plays a 
                        piece at the left-most available spot that it can.
                    </p>
                </li>
            </ul>
        </p>
        
        <h3>Configure and Play</h3>
        <hr/>
        <p>
            <c:url var="new_game_url" value="/connectfour/play" />
            <form:form method="POST" action="${new_game_url}">
                <input type="hidden" name="boardWidth" value="7" />
                <input type="hidden" name="boardHeight" value="6" />
                <input type="hidden" name="numberInRowToWin" value="4" />
                <input type="hidden" name="playerOneType" value="pc" />
                <input type="hidden" name="playerTwoType" value="npc-left-to-right-agent" />
                <input type="submit" value="Play with Defaults" />
            </form:form>
        </p>
    </div>
</div>
    
<%@ include file="/WEB-INF/views/footer.jsp" %>
