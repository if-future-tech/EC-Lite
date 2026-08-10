<%@ page contentType="text/html; charset=UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="ja">

        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>EC-Lite | ログイン</title>
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
                    max-width: 400px;
                    margin: 80px auto;
                    background: #fff;
                    border: 1px solid var(--border-color);
                    border-radius: 6px;
                    padding: 40px;
                    text-align: center;
                }

                .google-button {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 10px;
                    width: 100%;
                    padding: 10px 16px;
                    border: 1px solid var(--border-color);
                    border-radius: 4px;
                    background: #fff;
                    font-size: 14px;
                    cursor: pointer;
                    margin-top: 16px;
                }

                .google-button:hover {
                    background: #f5f5f5;
                }

                .google-button:disabled {
                    opacity: 0.6;
                    cursor: default;
                }

                .error {
                    background: #fdecea;
                    color: #b3261e;
                    padding: 10px;
                    border-radius: 4px;
                    margin-bottom: 16px;
                    font-size: 14px;
                    text-align: left;
                }

                .status {
                    font-size: 13px;
                    color: #777;
                    margin-top: 16px;
                    min-height: 18px;
                }
            </style>
        </head>

        <body>
            <!-- ec.lite login v2 marker -->
            <header>
                <h1>EC-Lite</h1>
            </header>

            <main>
                <h2>ログイン</h2>

                <c:if test="${not empty errorMessage}">
                    <div class="error">${errorMessage}</div>
                </c:if>

                <button id="google-login-button" class="google-button">
                    Googleでログイン
                </button>

                <div id="status" class="status"></div>
            </main>

            <!-- Firebase compat SDK（CDN読み込み。ビルドチェーン不要） -->
            <script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-app-compat.js"></script>
            <script src="https://www.gstatic.com/firebasejs/10.14.1/firebase-auth-compat.js"></script>

            <script>
                // ★ここに手順3で控えた firebaseConfig をそのまま貼り付ける
                const firebaseConfig = {
                    apiKey: "AIzaSyBS6hZOhINfj9P9IV9wZFOeHA2Gp05VuOI",
                    authDomain: "typing-ec-wp.firebaseapp.com",
                    projectId: "typing-ec-wp",
                    storageBucket: "typing-ec-wp.firebasestorage.app",
                    messagingSenderId: "659037616745",
                    appId: "1:659037616745:web:40000cd2b86c62ef3540f8",
                    measurementId: "G-4XB3VE79SR"
                };

                firebase.initializeApp(firebaseConfig);

                const contextPath = "${pageContext.request.contextPath}";
                const button = document.getElementById("google-login-button");
                const statusEl = document.getElementById("status");

                button.addEventListener("click", async () => {
                    button.disabled = true;
                    statusEl.textContent = "Googleアカウントを確認しています...";

                    try {
                        const provider = new firebase.auth.GoogleAuthProvider();
                        const result = await firebase.auth().signInWithPopup(provider);
                        const idToken = await result.user.getIdToken();

                        statusEl.textContent = "サーバーで検証しています...";

                        const params = new URLSearchParams();
                        params.set("idToken", idToken);

                        const response = await fetch(contextPath + "/login", {
                            method: "POST",
                            headers: { "Content-Type": "application/x-www-form-urlencoded" },
                            body: params.toString(),
                        });

                        const data = await response.json();

                        if (response.ok && data.success) {
                            window.location.href = data.redirectTo;
                        } else {
                            statusEl.textContent = data.errorMessage || "ログインに失敗しました。もう一度お試しください。";
                            button.disabled = false;
                        }
                    } catch (err) {
                        console.error(err);
                        statusEl.textContent = "";
                        button.disabled = false;
                        if (err.code === "auth/popup-closed-by-user") {
                            statusEl.textContent = "ログインがキャンセルされました。";
                        } else {
                            statusEl.textContent = "ログインに失敗しました。もう一度お試しください。";
                        }
                    }
                });
            </script>

        </body>

        </html>