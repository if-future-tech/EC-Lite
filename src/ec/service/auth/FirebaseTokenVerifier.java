package ec.service.auth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import ec.exception.BusinessException;

import java.net.URI;
import java.util.Date;
import java.util.Set;

public class FirebaseTokenVerifier {

    private static final String JWKS_URL = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

    private final String projectId;
    private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

    @SuppressWarnings("deprecation")
    public FirebaseTokenVerifier(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(
                    "Firebase projectId が設定されていません。ec.firebase.projectId（-Dフラグ）を確認してください。");
        }
        this.projectId = projectId;

        try {
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(URI.create(JWKS_URL).toURL());

            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256,
                    keySource);

            this.jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(keySelector);

            // iss / aud はFirebase固定値として厳密一致で検証する。
            // exp（期限切れ）はDefaultJWTClaimsVerifierが自動でチェックする。
            JWTClaimsSet exactMatchClaims = new JWTClaimsSet.Builder()
                    .issuer("https://securetoken.google.com/" + projectId)
                    .audience(projectId)
                    .build();
            Set<String> requiredClaims = Set.of("sub", "iat", "exp", "auth_time");

            jwtProcessor.setJWTClaimsSetVerifier(
                    new DefaultJWTClaimsVerifier<>(exactMatchClaims, requiredClaims));

        } catch (Exception e) {
            throw new IllegalStateException("JWKS初期化に失敗しました: " + JWKS_URL, e);
        }
    }

    public String getProjectId() {
        return projectId;
    }

    public DecodedIdentity verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BusinessException("認証トークンがありません");
        }

        JWTClaimsSet claims;
        try {
            claims = jwtProcessor.process(idToken, null);
        } catch (Exception e) {
            throw new BusinessException("認証トークンが無効です");
        }

        try {
            validateFirebaseSpecificClaims(claims);

            String uid = claims.getSubject();
            String email = claims.getStringClaim("email");
            String name = claims.getStringClaim("name");
            String picture = claims.getStringClaim("picture");

            return new DecodedIdentity(uid, email, name, picture);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("認証トークンが無効です");
        }
    }

    /**
     * Firebase公式ドキュメントが要求する追加チェック。
     * 標準JWTクレーム検証（iss/aud/exp）だけではFirebase固有の要件を満たさないため個別に確認する。
     * 参照:
     * https://firebase.google.com/docs/auth/admin/verify-id-tokens#verify_id_tokens_using_a_third-party_jwt_library
     */
    private static final long CLOCK_SKEW_MILLIS = 5000; // 5秒の許容誤差

    private void validateFirebaseSpecificClaims(JWTClaimsSet claims) throws Exception {
        String uid = claims.getSubject();
        if (uid == null || uid.isBlank() || uid.length() > 128) {
            throw new BusinessException("認証トークンが無効です");
        }

        Date authTime = claims.getDateClaim("auth_time");
        if (authTime == null) {
            throw new BusinessException("認証トークンが無効です");
        }
        // 秒単位クレームとミリ秒単位の現在時刻を比較するため、数秒の許容誤差を設ける
        if (authTime.getTime() > System.currentTimeMillis() + CLOCK_SKEW_MILLIS) {
            throw new BusinessException("認証トークンが無効です");
        }
    }

    public record DecodedIdentity(String uid, String email, String name, String pictureUrl) {
    }
}