<%@ include file="/WEB-INF/views/header.jsp" %>

<div class="container">
    <div style="margin-bottom:20px; font-size: 0.8em;">
        <a href="<c:url value="/" />">back to homepage</a>
    </div>
    
    ${boardHtml}
</div>
    
<%@ include file="/WEB-INF/views/footer.jsp" %>