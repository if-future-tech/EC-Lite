<%-- webapp/WEB-INF/jsp/orderHistory.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>EC-Lite | 注文履歴</title>
  <style>
    :root {
      --main-color: #2e7d32;
      --border-color: #e0e0e0;
      --bg-color: #fafafa;
    }

    body {
      margin: 0;
      font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      background: var(--bg-color);
      color: #333;
    }

    header {
      background: #fff;
      border-bottom: 1px solid var(--border-color);
      padding: 16px 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      position: sticky;
      top: 0;
      z-index: 100;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    }

    header h1 {
      font-size: 20px;
      margin: 0;
      color: var(--main-color);
    }

    header a {
      text-decoration: none;
      color: #333;
      font-weight: 500;
      margin-left: 16px;
    }

    main {
      padding: 24px;
      max-width: 900px;
      margin: 0 auto;
    }

    h2 {
      font-size: 18px;
    }

    .notice {
      font-size: 13px;
      color: #777;
      margin-bottom: 16px;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      background: #fff;
      border: 1px solid var(--border-color);
    }

    th, td {
      padding: 12px;
      border-bottom: 1px solid var(--border-color);
      text-align: left;
      font-size: 14px;
    }

    th {
      background: #f5f5f5;
      font-weight: 600;
    }

    .status-ordered {
      color: var(--main-color);
      font-weight: 600;
    }

    .status-canceled {
      color: #999;
    }

    .actions button {
      border: none;
      border-radius: 4px;
      padding: 6px 12px;
      font-size: 13px;
      cursor: pointer;
      margin-right: 6px;
    }

    .btn-cancel-active {
      background: #c62828;
      color: #fff;
    }

    .btn-disabled {
      background: #e0e0e0;
      color: #999;
      cursor: not-allowed;
    }

    .empty {
      padding: 40px;
      text-align: center;
      color: #777;
      background: #fff;
      border: 1px solid var(--border-color);
    }

    footer {
      margin-top: 40px;
      padding: 16px;
      text-align: center;
      font-size: 12px;
      color: #777;
    }
  </style>
</head>
<body>

<header>
  <h1>EC-Lite</h1>
  <div>
    <a href="${pageContext.request.contextPath}/products">商品一覧へ戻る</a>
    <a href="${pageContext.request.contextPath}/cart">カートを見る</a>
  </div>
</header>

<main>
  <h2>注文履歴（直近24時間）</h2>
  <p class="notice">
    キャンセルできるのは直近の注文1件のみです。24時間より前の注文についてはお問い合わせください。
  </p>

  <c:if test="${not empty errorMessage}">
    <div style="background:#fdecea; color:#b71c1c; padding:12px 16px;
            border-radius:4px; margin-bottom:16px; font-size:14px;
            border:1px solid #f5c6cb;">
      <c:out value="${errorMessage}" />
    </div>
  </c:if>

  <c:choose>
    <c:when test="${empty recentOrders}">
      <div class="empty">直近24時間以内の注文はありません。</div>
    </c:when>
    <c:otherwise>
      <table>
        <thead>
          <tr>
            <th>注文ID</th>
            <th>受付時刻</th>
            <th>ステータス</th>
            <th>発送状況</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="order" items="${recentOrders}">
            <tr>
              <td>${order.orderId}</td>
              <td><c:out value="${order.receptionAt}" /></td>
              <td>
                <c:choose>
                  <c:when test="${order.status == 'CANCELED'}">
                    <span class="status-canceled">キャンセル済み</span>
                  </c:when>
                  <c:otherwise>
                    <span class="status-ordered">注文確定</span>
                  </c:otherwise>
                </c:choose>
              </td>
              <td>
                <c:choose>
                  <c:when test="${order.shippingStatus == 'SHIPPED'}">発送済み</c:when>
                  <c:otherwise>未発送</c:otherwise>
                </c:choose>
              </td>
              <td class="actions">
                <c:choose>
                  <c:when test="${order.orderId == cancelableOrderId}">
                    <form method="post" action="${pageContext.request.contextPath}/order/cancel" style="display:inline;">
                      <button type="submit" class="btn-cancel-active">キャンセル</button>
                    </form>
                  </c:when>
                  <c:otherwise>
                    <button type="button" class="btn-disabled" disabled>キャンセル</button>
                  </c:otherwise>
                </c:choose>

                <c:choose>
                  <c:when test="${order.shippingStatus == 'SHIPPED'}">
                    <form method="post" action="${pageContext.request.contextPath}/order/return" style="display:inline;">
                      <input type="hidden" name="returnTo" value="history" />
                      <button type="submit" class="btn-cancel-active">返品申請</button>
                    </form>
                  </c:when>
                  <c:otherwise>
                    <button type="button" class="btn-disabled" disabled title="発送済みの注文のみ対象">
                      返品申請
                    </button>
                  </c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>

      </table>
    </c:otherwise>
  </c:choose>
</main>

<footer>
  © EC-Lite Sample UI
</footer>

</body>
</html>
