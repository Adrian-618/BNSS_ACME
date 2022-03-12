# BNSS_ACME

This is the implementation of NSS-VPKI for EP2520 - BNSS.

Below are instructions for using the system. We assume you already have the whole software and configuration running on each virtual machines. For detailed guidance on how to install the system, please visit our [wiki]([Home · Adrian-618/BNSS_ACME Wiki (github.com)](https://github.com/Adrian-618/BNSS_ACME/wiki))

### For Administrators

To add a new user, follows: 

1. go to the FreeIPA Web UI (https://ipasrv.auth.local) and add a new user. Enter the necessary information and click **ADD**.

2. Edit the .inf file to define the unique data of the user, the only thing need to change is the email and commonName:

   ```
   [ req ]
   prompt = no
   distinguished_name  = client
   default_bits  = 2048
   
   [ client ]
   countryName  = SE
   stateOrProvinceName = Stockholm
   localityName  = Stockholm
   organizationName    = AUTH.LOCAL
   emailAddress = wangzk@auth.local
   commonName  = wangzk
   ```

   

3. Using the OpenSSL to create the necessary material needed:

   ```
   openssl req -new -key client.key -out client.csr -config client.inf
   ```

4. Paste the csr to the FreeIPA and generate a new Certificate(Actions->New Certificate), and then download it.

5. Using this Command to combine to a PKCS12 Certificate:

   ```
   openssl pkcs12 -export -in wangzk.pem -inkey wangzk.key -certfile ca.pem -name wangzk -out wangzk.p12 -passin pass:time2work -passout pass:time2work
   ```

6. distribute the Certificate to the new user, with his username and password

7. Generate a new 2FA token for users(Actions->New OTP Token), and send the link to user

### For users

First Thing to do:

1. check the username and password, and install the PKCS12 certificate to the computer and mobile phone
2. using the OTP link to import the OTP to the FreeOTP,  which should have been installed on the mobile.
3. Getting the openvpn client configure file client.ovpn from the website of the company

To access the internal network:

1. Setting the encryption mode to TLS, and select your certificate, domain name fill "radius" and connect
2. Or, using your username and password attached with the OTP

To use OpenVPN:

1. import the client.ovpn file to your openvpn client
2. click connect and select your certificate to login
3. Or, you can go to the website (https://websrv.auth.local) and enter your username/password, download the ovpn and use it to connect

To use NextCloud:

1. Login to the NextCloud using the password
2. Enjoy it, share your files with anyone else~