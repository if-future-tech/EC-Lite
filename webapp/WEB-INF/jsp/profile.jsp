<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="ja">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>EC-Lite | プロフィール</title>
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
                    position: sticky;
                    top: 0;
                    z-index: 100;
                    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
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
                    margin-left: 12px;
                }

                main {
                    max-width: 480px;
                    margin: 40px auto;
                    background: #fff;
                    border: 1px solid var(--border-color);
                    border-radius: 6px;
                    padding: 32px;
                }

                .avatar {
                    width: 72px;
                    height: 72px;
                    border-radius: 50%;
                    background: #ddd;
                    margin: 0 auto 16px;
                    display: block;
                    object-fit: cover;
                }

                label {
                    display: block;
                    font-size: 13px;
                    font-weight: 600;
                    margin: 16px 0 4px;
                }

                input[type=text] {
                    width: 100%;
                    padding: 8px;
                    border: 1px solid var(--border-color);
                    border-radius: 4px;
                    box-sizing: border-box;
                    font-size: 14px;
                }

                .save-button {
                    margin-top: 24px;
                    width: 100%;
                    background: var(--main-color);
                    color: #fff;
                    border: none;
                    border-radius: 4px;
                    padding: 10px;
                    font-size: 15px;
                    cursor: pointer;
                }

                .error {
                    background: #fdecea;
                    color: #b3261e;
                    padding: 10px;
                    border-radius: 4px;
                    margin-bottom: 16px;
                    font-size: 14px;
                }

                .success {
                    background: #e8f5e9;
                    color: #2e7d32;
                    padding: 10px;
                    border-radius: 4px;
                    margin-bottom: 16px;
                    font-size: 14px;
                }

                .readonly-field {
                    font-size: 13px;
                    color: #777;
                    margin-top: 4px;
                }
            </style>
        </head>

        <body>

            <header>
                <h1>EC-Lite</h1>
                <div>
                    <a href="${pageContext.request.contextPath}/products">商品一覧へ戻る</a>
                    <a href="${pageContext.request.contextPath}/logout">ログアウト</a>
                </div>
            </header>

            <main>
                <h2>プロフィール編集</h2>

                <c:if test="${not empty errorMessage}">
                    <div class="error">${errorMessage}</div>
                </c:if>
                <c:if test="${not empty successMessage}">
                    <div class="success">${successMessage}</div>
                </c:if>

                <img class="avatar"
                    src="${not empty sessionScope.currentUser.iconUrl ? sessionScope.currentUser.iconUrl : ''}"
                    alt="アイコン" />

                <div class="readonly-field">メールアドレス：${sessionScope.currentUser.email}</div>

                <form method="post" action="${pageContext.request.contextPath}/profile">
                    <label>表示名</label>
                    <input type="text" name="displayName" value="${sessionScope.currentUser.displayName}" required />

                    <label>アイコンURL</label>
                    <input type="text" name="iconUrl" value="${sessionScope.currentUser.iconUrl}" />

                    <label>電話番号（任意）</label>
                    <input type="text" name="phone" value="${sessionScope.currentUser.phone}" />

                    <label>郵便番号（任意）</label>
                    <input type="text" name="postalCode" value="${sessionScope.currentUser.postalCode}" />

                    <label>配送先住所（任意）</label>
                    <input type="text" name="address" value="${sessionScope.currentUser.address}" />

                    <button type="submit" class="save-button">保存する</button>
                </form>
            </main>

        </body>

        </html>