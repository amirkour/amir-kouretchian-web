<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="container">
    <div class="banner">
        <h2>Amir Kouretchian</h2>
        <ul class="links">
            <li><a href="https://github.com/amirkour">on github</a></li>
            <li><a href="https://www.linkedin.com/pub/amir-kouretchian/9/6a1/383">on linkedin</a></li>
        </ul>
        <img id="mug-shot" alt="me!" src="<c:url value="/img/me.jpg" />" />
        
    </div>
    
    <div class="body">
        <h3>About Me</h3>
        <hr/>
        <p>
        Hi, my name is Amir Kouretchian and I'm a software engineer with a passion for design patterns, architecture, and software best practices.
        </p>
        <p>
        I've spent the better part of my career up-and-down the web stack with a bias/emphasis towards the middle and front.
        </p>
        <p>
        I've built web interfaces to robust data models backed by relational and object databases, single-page apps backed by REST services,
        old ASP.NET web-forms applications, and a slew of other various tools/infrastructure projects.
        </p>
        <p>
        Nowadays, I'm playing around with artificial intelligence in the context of zero-sum, strategy board games.  In particular, building
        software agents that can play in teams, or against, each other and in concert with humans on the web in turn-based games.
        </p>
    </div>
        
    <div class="body">
        <h3>Current Projects</h3>
        <hr/>
        <ul>
            <li>
                <p>
                <span class="project-title">Connect-Four Game Agents</span>
                    I used the classic Hasbro (previously Milton Bradley) game <a href="http://www.hasbro.com/en-us/toys-games/hasbro-games:connect-4">connect 4</a>
                    as a forum to begin playing with some of the AI algorithms I've been learning (as well as a way to get back into Java.)
                </p>
                <ul class="links">
                    <li><a href="<c:url value="/connectfour" />">play now</a></li>
                    <li><a href="https://github.com/amirkour/connect-four">github</a></li>
                </ul>
            </li>
        </ul>
    </div>
    
</div>
    
<%@ include file="/WEB-INF/views/footer.jsp" %>
