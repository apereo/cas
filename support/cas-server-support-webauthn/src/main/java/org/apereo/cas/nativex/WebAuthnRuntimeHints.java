package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.webauthn.WebAuthnCredentialRegistrationCipherExecutor;
import com.yubico.data.AssertionResponse;
import com.yubico.data.CredentialRegistration;
import com.yubico.data.RegistrationRequest;
import com.yubico.data.RegistrationResponse;
import com.yubico.fido.metadata.AAGUID;
import com.yubico.fido.metadata.AAID;
import com.yubico.fido.metadata.AlternativeDescriptions;
import com.yubico.fido.metadata.AttachmentHint;
import com.yubico.fido.metadata.AuthenticationAlgorithm;
import com.yubico.fido.metadata.AuthenticatorAttestationType;
import com.yubico.fido.metadata.AuthenticatorGetInfo;
import com.yubico.fido.metadata.AuthenticatorStatus;
import com.yubico.fido.metadata.BiometricAccuracyDescriptor;
import com.yubico.fido.metadata.BiometricStatusReport;
import com.yubico.fido.metadata.CodeAccuracyDescriptor;
import com.yubico.fido.metadata.CtapCertificationId;
import com.yubico.fido.metadata.CtapPinUvAuthProtocolVersion;
import com.yubico.fido.metadata.CtapVersion;
import com.yubico.fido.metadata.DisplayPNGCharacteristicsDescriptor;
import com.yubico.fido.metadata.ExtensionDescriptor;
import com.yubico.fido.metadata.FidoMetadataService;
import com.yubico.fido.metadata.MetadataBLOBHeader;
import com.yubico.fido.metadata.MetadataBLOBPayload;
import com.yubico.fido.metadata.MetadataBLOBPayloadEntry;
import com.yubico.fido.metadata.MetadataStatement;
import com.yubico.fido.metadata.PatternAccuracyDescriptor;
import com.yubico.fido.metadata.ProtocolFamily;
import com.yubico.fido.metadata.PublicKeyRepresentationFormat;
import com.yubico.fido.metadata.RgbPaletteEntry;
import com.yubico.fido.metadata.StatusReport;
import com.yubico.fido.metadata.SupportedCtapOptions;
import com.yubico.fido.metadata.TransactionConfirmationDisplayType;
import com.yubico.fido.metadata.VerificationMethodDescriptor;
import com.yubico.fido.metadata.Version;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.attestation.AttestationTrustSource;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.COSEAlgorithmIdentifier;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.extension.uvm.KeyProtectionType;
import com.yubico.webauthn.extension.uvm.MatcherProtectionType;
import com.yubico.webauthn.extension.uvm.UserVerificationMethod;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;

/**
 * This is {@link WebAuthnRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WebAuthnRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(
            MetadataBLOBHeader.class,
            MetadataBLOBHeader.MetadataBLOBHeaderBuilder.class,
            MetadataBLOBPayload.class,
            MetadataBLOBPayload.MetadataBLOBPayloadBuilder.class,
            CredentialRegistration.class,
            CredentialRegistration.CredentialRegistrationBuilder.class,
            MetadataBLOBPayloadEntry.class,
            MetadataBLOBPayloadEntry.MetadataBLOBPayloadEntryBuilder.class,
            MetadataStatement.class,
            MetadataStatement.MetadataStatementBuilder.class,
            SupportedCtapOptions.class,
            SupportedCtapOptions.SupportedCtapOptionsBuilder.class,
            AuthenticatorGetInfo.class,
            AuthenticatorGetInfo.AuthenticatorGetInfoBuilder.class,
            TypeReference.of("com.yubico.fido.metadata.AuthenticatorGetInfo$SetFromIntJsonDeserializer"),
            TypeReference.of("com.yubico.fido.metadata.AuthenticatorGetInfo$IntFromSetJsonSerializer"),
            RegistrationRequest.class,
            RegistrationResponse.class,
            AssertionResponse.class,
            RegisteredCredential.RegisteredCredentialBuilder.class,
            RegisteredCredential.class,
            WebAuthnCredentialRegistrationCipherExecutor.class,
            TypeReference.of("com.yubico.fido.metadata.CertFromBase64Converter"),
            FidoMetadataService.class,
            FidoMetadataService.FidoMetadataServiceBuilder.class,
            AttestationTrustSource.class,
            AttestationTrustSource.TrustRootsResult.TrustRootsResultBuilder.class,
            StatusReport.class,
            ProtocolFamily.class,
            Attestation.class,
            AuthenticatorStatus.class,
            AuthenticatorAttestationType.class,
            AttachmentHint.class,
            RelyingPartyIdentity.class,
            UserIdentity.class,
            UserVerificationMethod.class,
            UserVerificationRequirement.class,
            AlternativeDescriptions.class,
            KeyProtectionType.class,
            RgbPaletteEntry.class,
            MatcherProtectionType.class,
            AAGUID.class,
            AAID.class,
            ByteArray.class,
            Version.class,
            PublicKeyCredentialParameters.class,
            PublicKeyCredentialType.class,
            COSEAlgorithmIdentifier.class,
            CtapVersion.class,
            CtapPinUvAuthProtocolVersion.class,
            CtapCertificationId.class,
            AuthenticationAlgorithm.class,
            PublicKeyRepresentationFormat.class,
            TransactionConfirmationDisplayType.class,
            StatusReport.StatusReportBuilder.class,
            DisplayPNGCharacteristicsDescriptor.class,
            DisplayPNGCharacteristicsDescriptor.DisplayPNGCharacteristicsDescriptorBuilder.class,
            VerificationMethodDescriptor.class,
            VerificationMethodDescriptor.VerificationMethodDescriptorBuilder.class,
            BiometricAccuracyDescriptor.class,
            BiometricAccuracyDescriptor.BiometricAccuracyDescriptorBuilder.class,
            PatternAccuracyDescriptor.class,
            PatternAccuracyDescriptor.PatternAccuracyDescriptorBuilder.class,
            CodeAccuracyDescriptor.class,
            CodeAccuracyDescriptor.CodeAccuracyDescriptorBuilder.class,
            BiometricStatusReport.class,
            BiometricStatusReport.BiometricStatusReportBuilder.class,
            ExtensionDescriptor.class,
            ExtensionDescriptor.ExtensionDescriptorBuilder.class
        ));
    }
}
