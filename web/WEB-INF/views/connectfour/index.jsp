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
            I decided to take a foray into artificial intelligence (at the awesome 
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
            <ul style="list-style:disc; padding-left:20px;">
                <li>a (configurable) depth-limited Minimax Agent</li>
                <li>a dummy/test agent</li>
            </ul>
            <p>
            Use the section below to configure/play against one of these agents, or to watch them
            battle it out against one-another (which is surprisingly satisfying.)
            </p>
            <p>
            For information on Minimax, see wikipedia's description <a href="https://en.wikipedia.org/wiki/Minimax">here.</a>
            </p>
        </p>
        
        <h3>Configure and Play</h3>
        <hr/>
        <p>
            TODO
        </p>
    </div>
</div>
    
<%@ include file="/WEB-INF/views/footer.jsp" %>
