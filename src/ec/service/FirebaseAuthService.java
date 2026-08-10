package ec.service;

import ec.model.User;
import ec.repository.FirebaseUserRepository;
import ec.service.auth.FirebaseTokenVerifier;
import ec.service.auth.FirebaseTokenVerifier.DecodedIdentity;

public class FirebaseAuthService {

    private final FirebaseUserRepository userRepo;
    private final FirebaseTokenVerifier verifier;

    public FirebaseAuthService(FirebaseUserRepository userRepo, FirebaseTokenVerifier verifier) {
        this.userRepo = userRepo;
        this.verifier = verifier;
    }

    public User loginWithIdToken(String idToken) {
        DecodedIdentity identity = verifier.verify(idToken);

        User existing = userRepo.findByFirebaseUid(identity.uid());
        if (existing != null) {
            userRepo.updateLastLogin(existing);
            return existing;
        }

        User newUser = User.newFirebaseUser(
                identity.uid(), identity.email(), identity.name(), identity.pictureUrl());
        return userRepo.save(newUser);
    }
}