# ============================================================
# EC-Lite v2.0 Dockerfile
# Stage 1: ソースからコンパイル（コンテナ内で毎回ゼロからビルドし、
#          ローカルのwebapp/WEB-INF/classesの中身には一切依存しない）
# Stage 2: Tomcatにデプロイして実行
# ============================================================

# ── Stage 1: コンパイル ──
FROM eclipse-temurin:25-jdk AS build
WORKDIR /build

COPY lib/ lib/
COPY src/ src/

# ローカルと同じコマンド（--release 24）でバイトコードバージョンを固定する
RUN mkdir -p /build/out && \
    javac -cp "lib/*" --release 24 -d /build/out $(find src -name "*.java")

# ── Stage 2: 実行用Tomcatイメージ ──
FROM tomcat:10.1-jdk25-temurin

# デフォルトのサンプルWebアプリ（ROOT/docs/examples等）を削除
RUN rm -rf /usr/local/tomcat/webapps/*

# ROOTコンテキストとしてデプロイ（Cloud Runはサービス単位でURLが決まるため
# /ec-lite というコンテキストパスを持たせる意味が薄い。JSP側は
# ${pageContext.request.contextPath} ベースでリンクを書いているので
# コード変更なしでそのまま対応できる）
COPY webapp/ /usr/local/tomcat/webapps/ROOT/

# ローカルのビルド成果物（古い可能性がある）を消してから、
# コンテナ内で今ビルドしたばかりのクラスファイルだけを配置する
RUN rm -rf /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/*
COPY --from=build /build/out/ /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/

# Cloud Run向けの低メモリ設定（実行プラン §3.5）
# Cloud Run側のメモリ割り当て(例:512Mi)の7割程度を目安にする
ENV CATALINA_OPTS="-Xmx350m -Xms128m"

EXPOSE 8080
