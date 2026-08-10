<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="ja">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>EC-Lite | カート</title>
            <style>
                /* ─── モックの <style> をそのまま ─── */
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
                }

                main {
                    padding: 24px;
                    max-width: 900px;
                    margin: 0 auto;
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

                .price {
                    color: var(--main-color);
                    font-weight: 600;
                }

                .quantity input {
                    width: 60px;
                    padding: 4px;
                }

                .summary {
                    margin-top: 24px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                .total {
                    font-size: 18px;
                    font-weight: 600;
                }

                .order-button {
                    background: var(--main-color);
                    color: #fff;
                    border: none;
                    border-radius: 4px;
                    padding: 10px 20px;
                    font-size: 15px;
                    cursor: pointer;
                }

                footer {
                    margin-top: 40px;
                    padding: 16px;
                    text-align: center;
                    font-size: 12px;
                    color: #777;
                }

                .cart-actions {
                    display: flex;
                    gap: 8px;
                    align-items: center;
                }

                .cart-actions input[type=number] {
                    width: 60px;
                }

                /* ★新規: 直近注文サマリー */
                .order-summary {
                    margin-top: 24px;
                    padding: 16px;
                    background: #fff;
                    border: 1px solid var(--border-color, #e0e0e0);
                    border-radius: 6px;
                }

                .order-summary h3 {
                    margin: 0 0 12px 0;
                    font-size: 15px;
                }

                .order-summary-row {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 16px;
                    align-items: center;
                    font-size: 14px;
                }

                .order-summary-row .label {
                    color: #777;
                    font-size: 12px;
                    display: block;
                }

                .order-summary-empty {
                    font-size: 13px;
                    color: #777;
                }

                .order-summary-actions {
                    margin-left: auto;
                    display: flex;
                    gap: 8px;
                }

                .order-summary-actions button {
                    border: none;
                    border-radius: 4px;
                    padding: 6px 12px;
                    font-size: 13px;
                    cursor: pointer;
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

                .order-summary-link {
                    margin-top: 8px;
                    font-size: 12px;
                }
            </style>
        </head>

        <body>
            <header>
                <h1>EC-Lite</h1>
                <div style="display:flex; align-items:center; gap:16px;">
                    <c:choose>
                        <c:when test="${not empty sessionScope.currentUser}">
                            <span style="font-size:14px;">
                                ようこそ、
                                <c:out value="${sessionScope.currentUser.displayLabel}" /> さん
                            </span>
                            <a href="${pageContext.request.contextPath}/orders/history">注文履歴</a>
                            <a href="${pageContext.request.contextPath}/profile">プロフィール</a>
                            <a href="${pageContext.request.contextPath}/logout">ログアウト</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/login">ログイン</a>
                        </c:otherwise>
                    </c:choose>
                    <a href="${pageContext.request.contextPath}/products">商品一覧へ戻る</a>
                </div>
            </header>
            <main>
                <h2>カート内容</h2>

                <%-- BusinessException発生時（再注文制御・在庫不足・キャンセル不可等）のエラー表示 --%>
                    <c:if test="${not empty errorMessage}">
                        <div style="background:#fdecea; color:#b71c1c; padding:12px 16px;
                                border-radius:4px; margin-bottom:16px; font-size:14px;
                                border:1px solid #f5c6cb;">
                            <c:out value="${errorMessage}" />
                        </div>
                    </c:if>

                    <c:if test="${not empty cancelMessage}">
                        <div style="background:#e6f4ea; color:#1e4620; padding:12px 16px;
                                border-radius:4px; margin-bottom:16px; font-size:14px;
                                border:1px solid #b7dfc0;">
                            <c:out value="${cancelMessage}" />
                        </div>
                    </c:if>

                    <table>
                        <thead>
                            <tr>
                                <th>商品名</th>
                                <th>単価</th>
                                <th>数量</th>
                                <th>小計</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="total" value="${0}" />
                            <c:forEach var="item" items="${cart.items}">
                                <c:set var="subtotal" value="${item.product.price * item.quantity}" />
                                <c:set var="total" value="${total + subtotal}" />
                                <tr>
                                    <td>
                                        <c:out value="${item.product.name}" />
                                    </td>
                                    <td class="price">¥
                                        <c:out value="${item.product.price}" />
                                    </td>
                                    <td class="quantity">
                                        <form method="post" action="${pageContext.request.contextPath}/cart/update"
                                            class="cart-actions">
                                            <input type="hidden" name="productId" value="${item.product.id}" />
                                            <input type="number" name="quantity" value="${item.quantity}" min="1" />
                                            <button type="submit">更新</button>
                                        </form>
                                    </td>
                                    <td class="price">¥
                                        <c:out value="${subtotal}" />
                                    </td>
                                    <td>
                                        <form method="post" action="${pageContext.request.contextPath}/cart/remove">
                                            <input type="hidden" name="productId" value="${item.product.id}" />
                                            <button type="submit">削除</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <div class="summary">
                        <div class="total">合計金額：¥
                            <c:out value="${total}" />
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/order/confirm">
                            <button class="order-button" type="submit">注文を確定する</button>
                        </form>
                    </div>

                    <%-- ★変更: 無条件の「直近注文をキャンセル」ボタンを廃止し、
                         実データに基づく直近注文サマリー（orderHistory.jspの取得結果の1行ミラーリング）に置き換え。
                         未ログイン時はlatestRecentOrder自体が設定されないため、このブロックごと非表示になる。 --%>
                        <c:if test="${not empty sessionScope.currentUser}">
                            <div class="order-summary">
                                <h3>直近のご注文（24時間以内）</h3>

                                <c:choose>
                                    <c:when test="${empty latestRecentOrder}">
                                        <div class="order-summary-empty">直近24時間以内の注文はありません。</div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="order-summary-row">
                                            <div>
                                                <span class="label">注文ID</span>
                                                <c:out value="${latestRecentOrder.orderId}" />
                                            </div>
                                            <div>
                                                <span class="label">受付時刻</span>
                                                <c:out value="${latestRecentOrder.receptionAt}" />
                                            </div>
                                            <div>
                                                <span class="label">ステータス</span>
                                                <c:choose>
                                                    <c:when test="${latestRecentOrder.status == 'CANCELED'}">
                                                        キャンセル済み
                                                    </c:when>
                                                    <c:otherwise>
                                                        注文確定
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div>
                                                <span class="label">発送状況</span>
                                                <c:choose>
                                                    <c:when test="${latestRecentOrder.shippingStatus == 'SHIPPED'}">
                                                        発送済み
                                                    </c:when>
                                                    <c:otherwise>
                                                        未発送
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div class="order-summary-actions">
                                                <c:choose>
                                                    <c:when
                                                        test="${latestRecentOrder.orderId == cancelableOrderId}">
                                                        <form method="post"
                                                            action="${pageContext.request.contextPath}/order/cancel"
                                                            style="display:inline;">
                                                            <button type="submit"
                                                                class="btn-cancel-active">キャンセル</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="btn-disabled"
                                                            disabled>キャンセル</button>
                                                    </c:otherwise>
                                                </c:choose>

                                                <c:choose>
                                                    <c:when
                                                        test="${latestRecentOrder.shippingStatus == 'SHIPPED'}">
                                                        <form method="post"
                                                            action="${pageContext.request.contextPath}/order/return"
                                                            style="display:inline;">
                                                            <input type="hidden" name="returnTo" value="cart" />
                                                            <button type="submit"
                                                                class="btn-cancel-active">返品申請</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="btn-disabled" disabled
                                                            title="発送済みの注文のみ対象">
                                                            返品申請
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <div class="order-summary-link">
                                    <a href="${pageContext.request.contextPath}/orders/history">注文履歴をもっと見る</a>
                                </div>
                            </div>
                        </c:if>
            </main>
            <footer>
                © EC-Lite Sample UI
            </footer>
        </body>

        </html>
