<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="container">
    <div class="banner">
        <img id="mug-shot" alt="me!" src="<c:url value="/img/me.jpg" />" />
        <h2>Amir Kouretchian</h2>
        <ul class="links">
            <li><a href="https://github.com/amirkour">on github</a></li>
            <li><a href="https://www.linkedin.com/pub/amir-kouretchian/9/6a1/383">on linkedin</a></li>
        </ul>
    </div>
    
    <hr/>
    <div class="body">
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
        <p>
        To see what I'm tinkering with, see the links below.
        </p>
    </div>
    <hr/>
    
</div>
    
<%@ include file="/WEB-INF/views/footer.jsp" %>
