//import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;

public class LTCAClient {
    enum LTCA_OPCODE {
        REQ_VOUCHER_VEHICLE_TO_LTCA_USING_PROTO_BUFF                        (120),
        RES_VOUCHER_LTCA_TO_VEHICLE_USING_PROTO_BUFF                        (121),
        REQ_X509_CERT_REQ_VEHICLE_TO_LTCA_USING_PROTO_BUFF                  (122),
        RES_ISSUE_X509_CERT_LTCA_TO_VEHICLE_USING_PROTO_BUFF                (123),
        REQ_X509_CERT_VALIDATION_VEHICLE_TO_LTCA_USING_PROTO_BUFF           (124),
        RES_X509_CERT_VALIDATION_LTCA_TO_VEHICLE_USING_PROTO_BUFF           (125),
        REQ_NATIVE_TICKET_VEHICLE_TO_LTCA_USING_PROTO_BUFF                  (126),
        RES_NATIVE_TICKET_LTCA_TO_VEHICLE_USING_PROTO_BUFF                  (127),
        REQ_FOREIGN_TICKET_VEHICLE_TO_LTCA_USING_PROTO_BUFF                 (128),
        RES_FOREIGN_TICKET_LTCA_TO_VEHICLE_USING_PROTO_BUFF                 (129);

        private final int opCode;

        LTCA_OPCODE(int opCode) {
            this.opCode = opCode;
        }

        public int getOpCode() {
            return this.opCode;
        }

    }

    final static int LTCA_ID = 1002;
    final static String LTCA_OP = "ltca.operate";

    private String hostAddr = null;


    

    public LTCAClient(String hostAddr) {
        this.hostAddr = hostAddr;
        // InetAddressValidator validator = new InetAddressValidator();
        // if (validator.isValidInet4Address(hostAddr)) {
        //     this.hostAddr = hostAddr;
        // } else throw new IllegalArgumentException("Invalid IPv4 address.");

    }

    String call(String method, LTCA_OPCODE reqType, String encodedReq) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        String url = "https://" + hostAddr + "/cgi-bin/ltca";

        try {
            config.setServerURL(new URL(url));
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            Object[] params = new Object[] {
                    reqType.getOpCode(),
                    encodedReq
            };

            return (String) client.execute(method, params);
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL: " + url);
            System.err.println(e.toString());
        } catch (XmlRpcException e) {
            System.err.println(e.toString());
        }

        return null;
    }
    
}