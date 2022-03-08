import java.util.Arrays;
import java.util.Random;
import java.sql.Timestamp;
import java.security.cert.X509Certificate;

import its.service.interfaces.*;

import org.apache.commons.validator.routines.InetAddressValidator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Command(name = "PKIServices", version = "Version 0.1", mixinStandardHelpOptions = true)
public class PKIServices implements Runnable {

    @Option(names = { "-e", "--email" }, required = false, description = "Email address")
    String email = "ziyanq@kth.se";
    @Option(names = { "-lip", "--ltca-address"}, required = false, description = "LTCA IP Address")
    String ltca_address = "192.168.60.1";
    @Option(names = { "-cn", "--common-names" }, required = false, description = "Common names or IP addresses for X509 Cert.")
    String[] common_names = {};

    String ENCODED_VOUCHER;
    X509Certificate VOUCHER;

    String ENCODED_X509;
    X509Certificate X509CERT;

    String requestX509CN = "";

    @Override
    public void run() {
        if (!email.equals("") && !ltca_address.equals("")) {
            String out = String.format("Performing request with:\nEmail: %s\nLTCA Address: %s\nCN: %s\n", email, ltca_address, Arrays.toString(common_names));
            System.out.println(out);

            int commonIPCount = 0;
            int commonDNSCount = 0;
            StringBuilder commonIPSB = new StringBuilder();
            StringBuilder commonDNSSB = new StringBuilder();
            String wildCard = "";

            InetAddressValidator validator = new InetAddressValidator();
            for (String s: common_names) {
                boolean res = validator.isValidInet4Address(s);
                if (res) {
                    // Is valid IPv4
                    if (commonIPCount > 0) {
                        commonIPSB.append(",");
                    }

                    commonIPSB.append("IP.").append(++commonIPCount).append(":").append(s);
                } else {
                    if (commonDNSCount > 0) {
                        commonDNSSB.append(",");
                    }

                    if (s.contains("*")) {
                        if (wildCard.equals("")) {
                            wildCard = "DNS:" + s;
                        }
                    } else {
                        commonDNSSB.append("DNS.").append(++commonDNSCount).append(":").append(s);
                    }
                }
            }

            String commonDNS = commonDNSSB.toString();
            String commonIPs = commonIPSB.toString();
            if (commonDNSSB.length() > 0 || !wildCard.equals("")) {
                requestX509CN = commonDNS;

                if (!wildCard.equals("")) {
                    requestX509CN += wildCard;
                }

                if (commonIPs.length() > 0) {
                    requestX509CN += "," + commonIPs;
                }
            } else if (commonIPs.length() > 0) {
                requestX509CN = commonIPs;
            }

            System.out.println("Final: \n" + requestX509CN);

            LTCAClient ltcaClient = new LTCAClient(ltca_address);
            initTrust();
            //requestVoucher(ltcaClient); // Run this to get Voucher, not necessary.
            requestX509(ltcaClient, requestX509CN); // Run this to get X509 Cert.
            //requestTicket(ltcaClient); // Gives Error :(
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PKIServices()).execute(args);
        System.exit(exitCode);
    }


    void initTrust() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // Explicit trust i.e. always trust.
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // Explicit trust i.e. always trust.
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL"); // Install the all-trusting trust manager
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                }
            };
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {
            System.err.println("An error occurred during TrustManagement");
            System.err.println(e);
        }
    }



    void requestVoucher(LTCAClient ltcaClient) {
        Random random = new Random();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Interfaces.msgVoucherReq_V2LTCA.Builder voucherReq = Interfaces.msgVoucherReq_V2LTCA.newBuilder();

        voucherReq.setIReqType(LTCAClient.LTCA_OPCODE.REQ_VOUCHER_VEHICLE_TO_LTCA_USING_PROTO_BUFF.getOpCode());
        voucherReq.setStrUserName("");
        voucherReq.setStrPwd("");
        voucherReq.setStrEmailAddress(email);
        voucherReq.setStrCaptcha("captcha");
        voucherReq.setINonce(random.nextInt(65536));
        voucherReq.setTTimeStamp(timestamp.getTime());

        String encodedRequest = base64.encodeBase64(voucherReq.build().toByteArray());
        ENCODED_VOUCHER = ltcaClient.call(LTCAClient.LTCA_OP, LTCAClient.LTCA_OPCODE.REQ_VOUCHER_VEHICLE_TO_LTCA_USING_PROTO_BUFF, encodedRequest);
        String response = base64.decodeBase64(ENCODED_VOUCHER);

        base64.writeToFile(email + "_VOUCHER.crt", response);
        System.out.println("Acquired voucher for " + email + ":\nResponse:\n" + response.toString() + "\n\n" + ENCODED_VOUCHER + "\n");
    }


    void requestX509(LTCAClient ltcaClient, String cn) {
        Random random = new Random();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Interfaces.msgX509CertReq_V2LTCA.Builder certReq = Interfaces.msgX509CertReq_V2LTCA.newBuilder();

        certReq.setIReqType(LTCAClient.LTCA_OPCODE.REQ_X509_CERT_REQ_VEHICLE_TO_LTCA_USING_PROTO_BUFF.getOpCode());
        certReq.setILTCAIdRange(LTCAClient.LTCA_ID);
        certReq.setStrProofOfPossessionVoucher("");
        certReq.setStrDNSExtension(cn); // Alternatively, replace this according to "DNS.1:awesome.domain.com,DNS.2:domain.com,IP.1:192.168.1.1"
        certReq.setStrX509CertReq("MIIBLjCB1QIBADBzMQswCQYDVQQGEwJTRTETMBEGA1UECAwKU29tZS1TdGF0ZTEhMB8GA1UECgwYSW50ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMQ4wDAYDVQQDDAVaaXlhbjEcMBoGCSqGSIb3DQEJARYNeml5YW5xQGt0aC5zZTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBb+RCHW66YsU3Qq+jDxdxhEz2wOyDqf0FreKAyvDA9YF2OgF4/7o/Bl0ouT5ZcYqFU90omcbHS37mkQRhX2BrugADAKBggqhkjOPQQDAgNIADBFAiAh0HnYwTQojl6oZ0LHZG3dH6UyidBZ9kJI5ftZRdYgxAIhAPezCUT8ns4KlC78yu3LYChS5SvDC4d2Rm0ANHkDs7Er");  // Paste CSR as it is here, for example surrounded by """ """
        certReq.setINonce(random.nextInt(65536));
        certReq.setTTimeStamp(timestamp.getTime());

        String encodedReq = base64.encodeBase64(certReq.build().toByteArray());
        String encodedRes = ltcaClient.call(LTCAClient.LTCA_OP, LTCAClient.LTCA_OPCODE.REQ_X509_CERT_REQ_VEHICLE_TO_LTCA_USING_PROTO_BUFF, encodedReq);
        String response = base64.decodeBase64(encodedRes);

        base64.writeToFile(email + "_X509.crt", response);
        System.out.println("Acquired X509 for " + email + "\n" + "CN: " + Arrays.toString(common_names) + "\nResponse:\n" + response + "\n");
    }

    void requestTicket(LTCAClient ltcaClient) {
        Random random = new Random();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Interfaces.msgTicketReq.Builder ticketReq = Interfaces.msgTicketReq.newBuilder();

        ticketReq.setIReqType(LTCAClient.LTCA_OPCODE.REQ_NATIVE_TICKET_VEHICLE_TO_LTCA_USING_PROTO_BUFF.getOpCode());
        ticketReq.setUiServices(0);
        ticketReq.setUiPsnymCertNoRequest(0);
        ticketReq.setILTCAIdRange(LTCAClient.LTCA_ID);
        ticketReq.setIPCAIdRange(1001);
        ticketReq.setINonce(random.nextInt(65536));
        ticketReq.setTTimeStamp(timestamp.getTime());
        ticketReq.setStrX509Cert("");
        ticketReq.setTPsnymStartTime(System.currentTimeMillis());
        ticketReq.setTPsnymEndTime(System.currentTimeMillis() + 10000);

        String encodedReq = base64.encodeBase64(ticketReq.build().toByteArray());
        String encodedRes = ltcaClient.call(LTCAClient.LTCA_OP, LTCAClient.LTCA_OPCODE.REQ_NATIVE_TICKET_VEHICLE_TO_LTCA_USING_PROTO_BUFF, encodedReq);
        String response = base64.decodeBase64(encodedRes);

        base64.writeToFile(email + "_TICKET.crt", response);
        System.out.println("Acquired Ticket for " + email + "\nResponse:\n" + response + "\n");
    }
}