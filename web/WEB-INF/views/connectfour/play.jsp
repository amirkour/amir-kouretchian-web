<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="container">
    <div style="margin-bottom:20px; font-size: 0.8em;">
        <p><a href="<c:url value="/" />">back to homepage</a></p>
        <p><a href="<c:url value="/connectfour" />">back to connectfour agents</a></p>
    </div>
    
    <c:if test="${game.outcomeAlreadyDetermined()}">
        <c:url var="repeat_game_url" value="/connectfour/play/${game.id}/repeat" />
        
        <div class="alert alert-info">
            ${game.outcomeDescription}
            
            <form:form method="POST" action="${repeat_game_url}" style="margin-top:10px;">
                <input type="submit" value="Play Again!" />
            </form:form>
        </div>
    </c:if>
    
    ${boardHtml}
    
    <c:if test="${showInstructions}">
        <h4>Brief Instructions</h4>
        <hr/>
        <p>
            Click on an X to occupy an available/legal move on the board - you'll be alternating turns
            with the other player as you both try to get ${game.numberInRowToWin} in a row to win!
        </p>
    </c:if>
    
    <h4>Game Summary</h4>
    <hr/>
    <p>
    <c:forEach var="player" items="${game.players}" varStatus="status" >
        ${player.playerColor.name} (${player.playerType.name})
        <c:if test="${status.first}" >~VS~</c:if>
    </c:forEach>
    </p>
    
    <%-- if auto-posting is enabled, it means this game is between two AI.
         use some javascript to automatically most the next AIs move after
         a few seconds.
    
         only enable auto-post if no outcome has been determined for the game
    --%>
    <c:if test="${autoPost == true && !game.outcomeAlreadyDetermined()}">
            
        <p>${nextPlayerToMove.playerColor.name} is calculating it's move ...</p>
        <div class="hidden">
            <c:url var="make_move_url" value="/connectfour/play/${game.id}" />
            <form:form id="ai-auto-post-form" method="POST" action="${make_move_url}">
                <input type="hidden" name="gameId" value="${game.id}" />
                <input type="hidden" name="row" value="0" /><%-- row/col irrelevant for ai move - it gets calculated at runtime --%>
                <input type="hidden" name="col" value="0" /><%-- row/col irrelevant for ai move - it gets calculated at runtime --%>
                <input type="hidden" name="playerId" value="${nextPlayerToMove.id}" />
            </form:form>

            <script type="text/javascript">
                (function(window,document,undefined){
                    var aiAutoPostForm = document.getElementById("ai-auto-post-form"),
                        milliSecondsToPause = 2000;

                    setTimeout(function(){
                        aiAutoPostForm.submit();
                    }, milliSecondsToPause);
                })(window,document);
            </script>
        </div>
            
    </c:if>
</div>
    
<%@ include file="/WEB-INF/views/footer.jsp" %>