/*
 * Copyright (c) 2026 qiheng. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getboot.payment.infrastructure.wechatpay.security;

import com.getboot.payment.api.wechatpay.security.WechatPaySecurityService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.certificate.CertificateProvider;
import com.wechat.pay.java.core.http.HostName;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.http.HttpHeaders;
import com.wechat.pay.java.core.http.HttpRequest;
import com.wechat.pay.java.core.http.HttpResponse;
import com.wechat.pay.java.core.http.JsonResponseBody;
import com.wechat.pay.java.core.http.ResponseBody;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.certificate.CertificateService;
import com.wechat.pay.java.service.certificate.model.Data;
import com.wechat.pay.java.service.certificate.model.DownloadCertificateResponse;
import com.wechat.pay.java.service.certificate.model.EncryptCertificate;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信安全能力测试。
 *
 * @author qiheng
 */
class WechatPaySecurityServiceImplTest {

    private static final String API_V3_KEY = "0123456789abcdef0123456789abcdef";

    private static final String TEST_PRIVATE_KEY_PEM = """
            -----BEGIN PRIVATE KEY-----
            MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC7DmvcLsy/3A0H
            /hXLHqrBIo4ckmwFHdJW2/r0Wfe08V6D5vO8jB+k63NouE37k9acKfnNRK9c/W1G
            YedzqKyglygqbSMve0pwHtT8wgk5WNas45jxZfMOS8gWWy4pfSZAk7C95r6UNkOu
            uglHz8y4LEoxpg/5LsAv0sz3Xtei50ecrnbXgWyp447lcMlK2GJIGsSulpAKt3Se
            Zx6Bw27sc6GQyUdMLTXUico3TXC4NWWTCzfo0j5R2M4t9bwsQQFr+3IrL9BbFPU5
            GNgCoD+NWWNeePsYLHbz2Lz8fjaTDcQ5bjoW4O8M0ihV5FdqN0WELaE4eIfRt2q/
            uPFjQEtjAgMBAAECgf8h+fydgeKu9slnKj/LJUbezegCSJZWV5RdMpfiynSz6SPB
            0bF0Df+xdRMBcni3uaLBut4k6tTvjxjKL/5ag8jXYp1pF2nMthhPRUpqntQOFpTn
            0m91aY88iYdOiEFaR+Eq2ajA1ajIQxMIKcKFyLjmuyS+3+sG1/cQuLmCNTf13rxB
            jhlavwV9qiUxuXGFnyJVNOAJ+U2VVWv7uD6wl+d77f6JQiGkwx8qh73rYNBgStBS
            0VokSJN+sqiAZ1/PSUZLVJVEsdKwTGiAWPmuN8/LSzZ9tv3qH9GvbR3yTZgwjThZ
            xceBSNdsJwwGSW68cNTRvp8S1jqZVd9mqezPzIUCgYEA/gFLllnb/I6hvbmjqPc/
            wYYrZz7muNOLU/yqPScVrqNYhu0wWf4cgdydhLI81wThGZIvmowfUKvmDfpJEaUY
            FDBJeNN7n5M5ncorFhYnMgIXsRzicRqGSyQts7Uw3BDHsGCGYP4bX5nj6MFkGca4
            z4pkJyHEfDmJ3iELb99zIk0CgYEAvIaEtPIcfJCZJ0gLrhwvKqSIXtZeITksGWGl
            mkhM46Tb6UdeMql9WYY52nEilYw0+MkUdEtvqg0KrpZuCbJRwL8/CVCrbft9N4DK
            Gdrkc1OfP9Pg9KIrxEHuMIRNIL4HQy5Hefoj/ytkyD1Ubw+L2mS5SYqL16WWTsJD
            G8zkHG8CgYEA1jV3bmuHt1z+EAePer5Q+T7SObb1Uqesl368fugIRAgjb152aV6A
            4g+Qk8jQwDCwOSVsmfRTsG/XirApkQTe4p+6RnhriC7b5zMI1Q6HrYfQPSBz4xiQ
            aqwvEp/afax1C0zL5t5FbYFVkAQQDCDn2LcIMWwrzOulVjwd7hHOgf0CgYA/ezxs
            gao996Laq1PjiPII9eZFqQGBRnMnCM5uqTHa5cR3ROTfQltHLf+FItt2aAPeUjdl
            AZHUPRf/uf/LnBQTR1K+nD9dhXWgnulu01phyyKwp3P0bDc4msrN437lK5fec1L2
            K9i7L618D8rCqwV4wLnznYeBUezPEHLwhYm6WQKBgQDY5f+V5HjU6seCdQYstsyB
            tui+8Nd2LBNUxngN+jZ3iXR8H75lpx+NTUEBk/l0KbxYCiWsbcY7scWlyQRqpeXT
            M6bcXtXFMWwZuoILXHBbUM/uNl2XxdofbQ/0NahdvEWQIh/qDvgI/8ztgZjaeBm9
            AAgTUh6HlWP1Hrmqfs4dNw==
            -----END PRIVATE KEY-----
            """;

    private static final String TEST_CERTIFICATE_PEM = """
            -----BEGIN CERTIFICATE-----
            MIIDGTCCAgGgAwIBAgIUYWsXAfPEnbW923OZKFl8acsyuwowDQYJKoZIhvcNAQEL
            BQAwHDEaMBgGA1UEAwwRZ2V0Ym9vdC10ZXN0LWNlcnQwHhcNMjYwMzI5MTIwNjQx
            WhcNMjcwMzI5MTIwNjQxWjAcMRowGAYDVQQDDBFnZXRib290LXRlc3QtY2VydDCC
            ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALsOa9wuzL/cDQf+FcseqsEi
            jhySbAUd0lbb+vRZ97TxXoPm87yMH6Trc2i4TfuT1pwp+c1Er1z9bUZh53OorKCX
            KCptIy97SnAe1PzCCTlY1qzjmPFl8w5LyBZbLil9JkCTsL3mvpQ2Q666CUfPzLgs
            SjGmD/kuwC/SzPde16LnR5yudteBbKnjjuVwyUrYYkgaxK6WkAq3dJ5nHoHDbuxz
            oZDJR0wtNdSJyjdNcLg1ZZMLN+jSPlHYzi31vCxBAWv7cisv0FsU9TkY2AKgP41Z
            Y154+xgsdvPYvPx+NpMNxDluOhbg7wzSKFXkV2o3RYQtoTh4h9G3ar+48WNAS2MC
            AwEAAaNTMFEwHQYDVR0OBBYEFMyzxkb70wgEnj/w6g3resGBsuNHMB8GA1UdIwQY
            MBaAFMyzxkb70wgEnj/w6g3resGBsuNHMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZI
            hvcNAQELBQADggEBADvmhvbTyoUnqgWGthpF7z0DJXbMyR4xbScc4W8qPvcP4WB8
            uysQR6jNF2NFAHyVNZFGjbDLRJNxXq2paI3lRkrRZZBKmK9eR3zTEOxca29Y1Paq
            hcWUVrvsxfrgW1Yfz1xntAckjaJsVg7ET5p2ipBuwmnBi5YJTo+PucbjHaiKG/FX
            GtbvgR8brWrStqC7VnWQqdjGBNyLFbmyjEtSB1HSs8gvs1TwPckysZMQwQtR6kZq
            O3eSGFgUJLX33cN/4KQop+mgbKpDbkIpEWK7i9IymqAGnLt45cZ99/AYHVOe6dzB
            1SD6ce0+pz7ewFWdT7gmw44bexFHa9G8tJaALWw=
            -----END CERTIFICATE-----
            """;

    @Test
    void shouldEncryptSensitiveValueAndExposeWechatPaySerialNumber() throws Exception {
        X509Certificate certificate = PemUtil.loadX509FromString(TEST_CERTIFICATE_PEM);
        PrivateKey privateKey = PemUtil.loadPrivateKeyFromString(TEST_PRIVATE_KEY_PEM);
        RSAAutoCertificateConfig config = newConfig(privateKey, certificate);
        CertificateService certificateService = newCertificateService(new RecordingCertificateHttpClient(config));
        WechatPaySecurityService service = new WechatPaySecurityServiceImpl(config, certificateService);

        String plaintext = "18600001111";
        String ciphertext = service.encryptSensitiveValue(plaintext);

        assertNotEquals(plaintext, ciphertext);
        assertEquals(plaintext, decrypt(ciphertext, privateKey));
        assertEquals(certificate.getSerialNumber().toString(16).toUpperCase(), service.currentWechatPaySerialNumber());
        assertSame(certificateService, service.certificateService());
    }

    @Test
    void shouldDownloadPlatformCertificates() throws Exception {
        X509Certificate certificate = PemUtil.loadX509FromString(TEST_CERTIFICATE_PEM);
        PrivateKey privateKey = PemUtil.loadPrivateKeyFromString(TEST_PRIVATE_KEY_PEM);
        RSAAutoCertificateConfig config = newConfig(privateKey, certificate);
        RecordingCertificateHttpClient httpClient = new RecordingCertificateHttpClient(config);
        CertificateService certificateService = newCertificateService(httpClient);
        WechatPaySecurityService service = new WechatPaySecurityServiceImpl(config, certificateService);

        List<X509Certificate> certificates = service.downloadPlatformCertificates();

        assertEquals(1, certificates.size());
        assertEquals(certificate.getSerialNumber(), certificates.get(0).getSerialNumber());
        assertTrue(httpClient.lastRequest.getUrl().toString().endsWith("/v3/certificates"));
    }

    private static RSAAutoCertificateConfig newConfig(PrivateKey privateKey, X509Certificate certificate) throws Exception {
        RSAAutoCertificateConfig.Builder builder = new RSAAutoCertificateConfig.Builder()
                .merchantId("1900001234")
                .merchantSerialNumber("merchant-serial")
                .privateKey(privateKey)
                .apiV3Key(API_V3_KEY);
        Field certificateProviderField = RSAAutoCertificateConfig.Builder.class.getDeclaredField("certificateProvider");
        certificateProviderField.setAccessible(true);
        certificateProviderField.set(builder, new StaticCertificateProvider(certificate));

        Constructor<RSAAutoCertificateConfig> constructor =
                RSAAutoCertificateConfig.class.getDeclaredConstructor(RSAAutoCertificateConfig.Builder.class);
        constructor.setAccessible(true);
        return constructor.newInstance(builder);
    }

    private static CertificateService newCertificateService(HttpClient httpClient) throws Exception {
        Constructor<CertificateService> constructor =
                CertificateService.class.getDeclaredConstructor(HttpClient.class, HostName.class);
        constructor.setAccessible(true);
        return constructor.newInstance(httpClient, HostName.API);
    }

    private static String decrypt(String ciphertext, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static final class StaticCertificateProvider implements CertificateProvider {

        private final X509Certificate certificate;

        private StaticCertificateProvider(X509Certificate certificate) {
            this.certificate = certificate;
        }

        @Override
        public X509Certificate getCertificate(String serialNumber) {
            return certificate;
        }

        @Override
        public X509Certificate getAvailableCertificate() {
            return certificate;
        }
    }

    private static final class RecordingCertificateHttpClient implements HttpClient {

        private final RSAAutoCertificateConfig config;
        private HttpRequest lastRequest;

        private RecordingCertificateHttpClient(RSAAutoCertificateConfig config) {
            this.config = config;
        }

        @Override
        public <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType) {
            this.lastRequest = request;
            try {
                DownloadCertificateResponse response = new DownloadCertificateResponse();
                response.setData(List.of(newCertificateData(config)));
                return httpResponse(request, responseType.cast(response), "{\"data\":[]}");
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public InputStream download(String url) {
            return new ByteArrayInputStream(new byte[0]);
        }

        private static Data newCertificateData(RSAAutoCertificateConfig config) {
            EncryptCertificate encryptCertificate = new EncryptCertificate();
            encryptCertificate.setAlgorithm("AEAD_AES_256_GCM");
            encryptCertificate.setAssociatedData("certificate");
            encryptCertificate.setNonce("0123456789ab");
            encryptCertificate.setCiphertext(config.createAeadCipher().encrypt(
                    "certificate".getBytes(StandardCharsets.UTF_8),
                    "0123456789ab".getBytes(StandardCharsets.UTF_8),
                    TEST_CERTIFICATE_PEM.getBytes(StandardCharsets.UTF_8)
            ));

            Data data = new Data();
            data.setSerialNo("platform-serial");
            data.setEffectiveTime("2026-03-29T12:06:41+08:00");
            data.setExpireTime("2027-03-29T12:06:41+08:00");
            data.setEncryptCertificate(encryptCertificate);
            return data;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> HttpResponse<T> httpResponse(HttpRequest request, T serviceResponse, String body) throws Exception {
        Constructor<?> constructor = HttpResponse.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        HttpHeaders headers = new HttpHeaders();
        ResponseBody responseBody = new JsonResponseBody.Builder().body(body).build();
        if (constructor.getParameterCount() == 4) {
            return (HttpResponse<T>) constructor.newInstance(request, headers, responseBody, serviceResponse);
        }
        return (HttpResponse<T>) constructor.newInstance(request, headers, responseBody, serviceResponse, null);
    }
}
