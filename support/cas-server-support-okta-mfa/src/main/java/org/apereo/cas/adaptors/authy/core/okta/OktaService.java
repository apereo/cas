package org.apereo.cas.adaptors.authy.core.okta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.Clients;
import com.okta.sdk.helper.UserFactorApiHelper;
import com.okta.sdk.resource.api.UserApi;
import com.okta.sdk.resource.api.UserFactorApi;
import com.okta.sdk.resource.client.ApiClient;
import com.okta.sdk.resource.model.*;
import okhttp3.*;
import org.apereo.cas.adaptors.authy.config.OktaMfaProperties;
import org.apereo.cas.adaptors.authy.core.okta.custommodels.CustomVerifyUserFactorResponse;
import org.apereo.cas.adaptors.authy.core.okta.models.OktaFactorStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.okta.sdk.resource.model.FactorProvider.OKTA;
import static com.okta.sdk.resource.model.FactorType.PUSH;
import static com.okta.sdk.resource.model.FactorType.TOKEN_SOFTWARE_TOTP;
import static org.apereo.cas.adaptors.authy.core.Constants.*;


@Component("oktaService")
public class OktaService {
    private final UserApi userApi;
    private final UserFactorApi userFactorApi;

    private final OktaMfaProperties oktaMfaProperties;

    public OktaService(OktaMfaProperties oktaMfaProperties) {
        this.oktaMfaProperties = oktaMfaProperties;

        ApiClient oktaClient = Clients.builder()
                .setOrgUrl(oktaMfaProperties.getUrl())
                .setClientCredentials(new TokenClientCredentials(oktaMfaProperties.getToken()))
                .build();

        userApi = new UserApi(oktaClient);
        userFactorApi = new UserFactorApi(oktaClient);
    }

    public User findUserByEmail(String email) {
        System.out.println("****************** findUserByEmail ********************");
        List<User> userList = userApi.listUsers(email, null, null, null, null, null, null);
        if (userList != null) {
            return userList.iterator().next();
        }
        return null; // User not found
    }

    public List<UserFactor> listUserFactors(String userId) {
        return userFactorApi.listFactors(userId);
    }

    public UserFactor getFactor(String userId, String factorId) {
        return userFactorApi.getFactor(userId, factorId);
    }

    public UserFactor hasAnyFactorEnrolled(String userId, List<UserFactor> factors) {
        // Remove factors not in the ALLOWED_FACTORS list
        factors.removeIf(factor -> !Arrays.asList(ALLOWED_FACTORS).contains(factor.getFactorType().toString()));
        // Sort the factors based on the order in ALLOWED_FACTORS
        factors.sort(Comparator.comparingInt(f -> Arrays.asList(ALLOWED_FACTORS).indexOf(f.getFactorType().toString())));

        for (UserFactor f : factors) {
            if (f.getStatus().equals(FactorStatus.ACTIVE)) {
                // Challenge
                return f;
            } else {
                // Delete
                deleteFactor(userId, f.getId());
            }
        }
        // Enroll
        return null;
    }

    public OktaFactorStatus getFactorStatus(String userId, List<UserFactor> factors, FactorType factorType, FactorProvider factorProvider) {
        factors.sort(Comparator.comparingInt(f -> Arrays.asList(ALLOWED_FACTORS).indexOf(f.getFactorType().toString())));

        OktaFactorStatus oktaFactorStatus = null;
        for (UserFactor factor : factors) {
            if (factor.getFactorType().equals(factorType) && factor.getProvider().equals(factorProvider)) {
                if ((factor.getStatus().equals(FactorStatus.ACTIVE))) {
                    // Challenge
                    oktaFactorStatus = factor.getFactorType().equals(PUSH) ? new OktaFactorStatus(CHALLENGE_PUSH) : new OktaFactorStatus(CHALLENGE);
                } else {
                    // Delete & Enroll
                    deleteFactor(userId, factor.getId());
                    oktaFactorStatus = new OktaFactorStatus(getEnrollConstant(factorType, factorProvider));
                }
                oktaFactorStatus.setFactorId(factor.getId());
                break;
            }
        }
        // Enroll
        if (oktaFactorStatus == null)
            oktaFactorStatus = new OktaFactorStatus(getEnrollConstant(factorType, factorProvider));
        return oktaFactorStatus;
    }

    public UserFactor enrollEmailFactor(String userId, String email) {
        EmailUserFactorProfile emailProfile = new EmailUserFactorProfile();
        emailProfile.setEmail(email);
        EmailUserFactor emailFactor = new EmailUserFactor();
        emailFactor.setProvider(OKTA);
        emailFactor.setFactorType(FactorType.EMAIL);
        emailFactor.setProfile(emailProfile);

        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        return userFactorApiHelper.enrollFactorOfType(EmailUserFactor.class, userId, emailFactor, false, null, null, false);
    }

    public UserFactor enrollSmsFactor(String userId, String phoneNumber) {
        SmsUserFactorProfile smsProfile = new SmsUserFactorProfile();
        smsProfile.setPhoneNumber(phoneNumber);
        SmsUserFactor smsFactor = new SmsUserFactor();
        smsFactor.setProvider(OKTA);
        smsFactor.setFactorType(FactorType.SMS);
        smsFactor.setProfile(smsProfile);

        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        return userFactorApiHelper.enrollFactorOfType(SmsUserFactor.class, userId, smsFactor, false, null, null, false);
    }

    public UserFactor enrollTotpFactor(String userId, FactorProvider factorProvider) {
        TotpUserFactorProfile totpProfile = new TotpUserFactorProfile();
        TotpUserFactor totpFactor = new TotpUserFactor();
        totpFactor.setProvider(factorProvider);
        totpFactor.setFactorType(TOKEN_SOFTWARE_TOTP);
        totpFactor.setProfile(totpProfile);

        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        return userFactorApiHelper.enrollFactorOfType(TotpUserFactor.class, userId, totpFactor, false, null, null, false);
    }

    public UserFactor enrollPushFactor(String userId) {
        PushUserFactorProfile pushProfile = new PushUserFactorProfile();
        PushUserFactor pushFactor = new PushUserFactor();
        pushFactor.setProvider(OKTA);
        pushFactor.setFactorType(PUSH);
        pushFactor.setProfile(pushProfile);

        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        return userFactorApiHelper.enrollFactorOfType(PushUserFactor.class, userId, pushFactor, false, null, null, false);
    }

    public boolean activateFactor(String userId, String factorId, String enteredCode, boolean activatePush) {
        ActivateFactorRequest activateFactorRequest = new ActivateFactorRequest();
        activateFactorRequest.setPassCode(enteredCode);

        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        try {
            userFactorApiHelper.activateFactorOfType(CallUserFactor.class, userId, factorId, activateFactorRequest);
            if (activatePush) activatePushFactor(userId);
            return true;
        } catch (Exception e) {
            System.out.println("Error activating factor: " + e.getMessage());
            return false;
        }
    }

    private void activatePushFactor(String userId) {
        try {
            List<UserFactor> factors = listUserFactors(userId);
            for (UserFactor f : factors) {
                if (f.getFactorType().equals(FactorType.PUSH)) {
                    // Activate push
                    ActivateFactorRequest activateFactorRequest = new ActivateFactorRequest();
                    UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
                    userFactorApiHelper.activateFactorOfType(CallUserFactor.class, userId, f.getId(), activateFactorRequest);
                }
            }
        } catch (Exception e) {
            System.out.println("Error activating factor: " + e.getMessage());
        }
    }

    public void sendFactorChallenge(String userId, String factorId) {
        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        VerifyFactorRequest verifyFactorRequest = new VerifyFactorRequest();

        userFactorApiHelper.verifyFactor(userId, factorId, null, null, null, null, null, verifyFactorRequest);
    }

    public String sendPushFactorChallenge(String userId, String factorId) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(oktaMfaProperties.getUrl() + "/api/v1/users/" + userId + "/factors/" + factorId + "/verify")
                    .post(RequestBody.create("{}", MediaType.parse("application/json")))
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "SSWS " + oktaMfaProperties.getToken())
                    .build();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // To avoid errors if the object has no properties

            CustomVerifyUserFactorResponse factorResponse = mapper.readValue(responseBody, CustomVerifyUserFactorResponse.class);
            return factorResponse.getTransactionId();
        } catch (Exception e) {
            System.out.println("Error sending push challenge: " + e.getMessage());
            return null;
        }
    }

    public boolean verifyFactorChallenge(String userId, String factorId, String passCode) {
        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        VerifyFactorRequest verifyFactorRequest = new VerifyFactorRequest();
        verifyFactorRequest.setPassCode(passCode);

        try {
            VerifyUserFactorResponse response = userFactorApiHelper.verifyFactor(userId, factorId, null, null, null, null, null, verifyFactorRequest);
            return response.getFactorResult().getValue().equals("SUCCESS");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verifyPushFactorChallenge(String userId, String factorId, String transactionId) {
        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);

        try {
            VerifyUserFactorResponse transactionResponse = userFactorApiHelper.getFactorTransactionStatus(userId, factorId, transactionId);
            return transactionResponse.getFactorResult().getValue().equals("SUCCESS");
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteFactor(String userId, String factorId) {
        UserFactorApiHelper<UserFactor> userFactorApiHelper = new UserFactorApiHelper<>(userFactorApi);
        try {
            userFactorApiHelper.unenrollFactor(userId, factorId, null);
        } catch (Exception e) {
            System.out.println("Error deleting factor: " + e.getMessage());
        }
    }

    private String getEnrollConstant(FactorType factorType, FactorProvider factorProvider) {
        switch (factorType) {
            case SMS:
                return ENROLL_SMS;
            case EMAIL:
                return ENROLL_EMAIL;
            case PUSH:
                return ENROLL_OKTA_PUSH;
            case TOKEN_SOFTWARE_TOTP:
                switch (factorProvider) {
                    case OKTA:
                        return ENROLL_OKTA_TOTP;
                    case GOOGLE:
                        return ENROLL_GOOGLE;
                    default:
                        throw new IllegalArgumentException("Unsupported provider for TOKEN_SOFTWARE_TOTP: " + factorProvider);
                }
            default:
                throw new IllegalArgumentException("Unsupported factor type: " + factorType);
        }
    }
}