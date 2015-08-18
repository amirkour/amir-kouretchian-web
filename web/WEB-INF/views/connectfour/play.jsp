<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="container">
    <div style="margin-bottom:20px; font-size: 0.8em;">
        <p><a href="<c:url value="/" />">back to homepage</a></p>
        <p><a href="<c:url value="/connectfour" />">back to connectfour agents</a></p>
    </div>
    
    ${boardHtml}
</div>
    
<%@ include file="/WEB-INF/views/footer.jsp" %>