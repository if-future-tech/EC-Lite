<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="ja">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>EC-Lite | 注文完了</title>
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
                }

                header h1 {
                    font-size: 20px;
                    margin: 0;
                    color: var(--main-color);
                }

                main {
                    max-width: 600px;
                    margin: 80px auto;
                    background: #fff;
                    border: 1px solid var(--border-color);
                    border-radius: 6px;
                    padding: 40px;
                    text-align: center;
                }

                .message {
                    font-size: 18px;
                    margin-bottom: 24px;
                }

                .order-number {
                    font-size: 16px;
                    margin-bottom: 32px;
                }

                .order-number span {
                    font-weight: 600;
                    color: var(--main-color);
                }

                .back-link {
                    display: inline-block;
                    margin-top: 16px;
                    text-decoration: none;
                    color: #fff;
                    background: var(--main-color);
                    padding: 10px 20px;
                    border-radius: 4px;
                    font-size: 14px;
                }

                footer {
                    margin-top: 60px;
                    text-align: center;
                    font-size: 12px;
                    color: #777;
                }
            </style>
        </head>

        <body>

            <header>
                <h1>EC-Lite</h1>
            </header>

            <main>
                <div class="message">ご注文ありがとうございました。</div>

                <div class="order-number">
                    注文番号：<span>
                        <c:out value="${order.orderId}" />
                    </span>
                </div>

                <p>受付時刻：
                    <c:out value="${order.receptionAt}" />
                </p>

                <p>
                    ご注文内容の詳細は、登録されたメールアドレス宛に送信されます。
                </p>

                <a href="${pageContext.request.contextPath}/products" class="back-link">商品一覧へ戻る</a>
            </main>

            <footer>
                © EC-Lite Sample UI
            </footer>

        </body>

        </html>