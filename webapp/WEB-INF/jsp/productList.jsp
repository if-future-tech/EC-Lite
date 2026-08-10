<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
        <!DOCTYPE html>
        <html lang="ja">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>EC-Lite | 商品一覧</title>
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
                    max-width: 1000px;
                    margin: 0 auto;
                }

                .product-grid {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 24px;
                }

                .product-card {
                    background: #fff;
                    border: 1px solid var(--border-color);
                    border-radius: 6px;
                    padding: 16px;
                    display: flex;
                    flex-direction: column;
                }

                .product-image {
                    width: 100%;
                    height: 140px;
                    background: #ddd;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #666;
                    font-size: 14px;
                    margin-bottom: 12px;
                }

                .product-name {
                    font-size: 16px;
                    font-weight: 600;
                    margin-bottom: 8px;
                }

                .product-price {
                    color: var(--main-color);
                    font-weight: 600;
                    margin-bottom: 8px;
                }

                .product-stock {
                    font-size: 13px;
                    margin-bottom: 12px;
                    font-weight: 600;
                }

                .stock-ok {
                    color: var(--main-color);
                }

                .stock-low {
                    color: #e65100;
                }

                .stock-out {
                    color: #c62828;
                }

                .product-actions {
                    margin-top: auto;
                    display: flex;
                    gap: 8px;
                }

                .product-actions input {
                    width: 60px;
                    padding: 4px;
                }

                .product-actions button {
                    flex: 1;
                    background: var(--main-color);
                    color: #fff;
                    border: none;
                    border-radius: 4px;
                    padding: 6px 8px;
                    cursor: pointer;
                    font-size: 14px;
                }

                .product-actions button:disabled,
                .product-actions input:disabled {
                    background: #e0e0e0;
                    color: #999;
                    cursor: not-allowed;
                }

                footer {
                    margin-top: 40px;
                    padding: 16px;
                    text-align: center;
                    font-size: 12px;
                    color: #777;
                }

                /* 768px以下は2列、480px以下は1列に落とす（モックには無かったレスポンシブ対応の追加） */
                @media (max-width: 768px) {
                    .product-grid {
                        grid-template-columns: repeat(2, 1fr);
                    }
                }

                @media (max-width: 480px) {
                    .product-grid {
                        grid-template-columns: 1fr;
                    }
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
                    <a href="${pageContext.request.contextPath}/cart">カートを見る</a>
                </div>
            </header>
            <main>
                <h2>健康サプリ一覧</h2>

                <div class="product-grid">
                    <c:forEach var="product" items="${productList}">
                        <%-- ★商品ID→画像ファイル名の対応表。Productモデルにimageフィールドが無いため、
                             View層に閉じたJSTLの対応表として持たせている（ドメインモデル変更なし） --%>
                        <c:set var="imageFile">
                            <c:choose>
                                <c:when test="${product.id == 1}">01_MultiVitamin.webp</c:when>
                                <c:when test="${product.id == 2}">02_Protein.webp</c:when>
                                <c:when test="${product.id == 3}">03_LacticAcidBacteria.webp</c:when>
                                <c:when test="${product.id == 4}">04_Aojiru.webp</c:when>
                                <c:when test="${product.id == 5}">05_DHA_EPA.webp</c:when>
                                <c:when test="${product.id == 6}">06_Glucosamine.webp</c:when>
                                <c:when test="${product.id == 7}">07_Collagen.webp</c:when>
                                <c:when test="${product.id == 8}">08_Iron.webp</c:when>
                                <c:when test="${product.id == 9}">09_Lutein.webp</c:when>
                                <c:otherwise></c:otherwise>
                            </c:choose>
                        </c:set>
                        <div class="product-card">
                            <div class="product-image">
                                <c:choose>
                                    <c:when test="${not empty imageFile}">
                                        <img src="${pageContext.request.contextPath}/images/${fn:trim(imageFile)}"
                                            alt="${product.name}"
                                            style="width:100%; height:100%; object-fit:cover;" />
                                    </c:when>
                                    <c:otherwise>Image</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="product-name">
                                <c:out value="${product.name}" />
                            </div>
                            <div class="product-price">¥
                                <c:out value="${product.price}" />
                            </div>
                            <%-- ★「残りわずか」の閾値。今回は固定値。将来的に業者ごとに設定可能にする場合は
                                 Productにthreshold項目を追加し、在庫補填メール送信をServiceに組み込む拡張が必要
                                 （README §17参照。今回はView層の表示切り替えのみ実装） --%>
                            <c:set var="lowStockThreshold" value="3" />
                            <div class="product-stock">
                                <c:choose>
                                    <c:when test="${product.stock le 0}">
                                        <span class="stock-out">在庫切れ</span>
                                    </c:when>
                                    <c:when test="${product.stock le lowStockThreshold}">
                                        <span class="stock-low">残りわずか（あと${product.stock}点）</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="stock-ok">在庫あり</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="product-actions">
                                <form method="post" action="${pageContext.request.contextPath}/cart/add">
                                    <input type="hidden" name="productId" value="${product.id}" />
                                    <c:choose>
                                        <c:when test="${product.stock le 0}">
                                            <input type="number" value="0" min="1" disabled />
                                            <button type="submit" disabled>追加</button>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="number" name="quantity" value="1" min="1" />
                                            <button type="submit">追加</button>
                                        </c:otherwise>
                                    </c:choose>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </main>
            <footer>
                © EC-Lite Sample UI
            </footer>
        </body>

        </html>
