## BNSS README

This readme is for tutorial for admins and users to use our system

## For Administrators

### Add a new user

To add a new user,  just SSH to the ipasrv.auth.local, and first edit the user.conf file, only need to edit the common name. Then run the script `/home/wangzk/bnss/AddNewUser.sh`. Follow the prompts to complete it. Then the administrator only needs to send the generated package to the user.

### Remove a user

Log in to the ipasrv.auth.local freeipa panel, delete the specific user and revoke its certificate

### Revoke old certificate and create a new one

Log in to the freeipa panel, revoke the certificate, and then using the script `/home/wangzk/bnss/createCertificate.sh [username]` to create a new certificate. Afterward, send the new issued certificate to the user.

### OpenVPN temporary ticket

Log in to the OpenVPN panel(`https://websrv.auth.local:943/admin`) as admin, then go to `user profiles` and add a new one to the specific user. When the user no longer needs it, remove it in the same way.

### NextCloud Control

Actually the administrator doesn't need to do anything, if there's a folder needing to be shared with all users, just share it with the `ipausers` group, then all the users should be able to access it.

## For users

### Prerequisites

1. check the username and password, and install the PKCS12 certificate to the computer and mobile phone
2. using the OTP link to import the OTP to the FreeOTP,  which should have been installed on the mobile.
3. Getting the openvpn client configure file client.ovpn from the website of the company
4. Login to the FreeIPA UI to change the password to your own

### Access the internal network

1. Setting the encryption mode to TLS, and select your certificate, domain name fill "radius" and connect
2. Or, using your username and password

### To use OpenVPN

1. import the client.ovpn file to your openvpn client. Then click connect and select your certificate to login
3. Or, you can go to the website (https://websrv.auth.local) and enter your username/password, download the ovpn and use it to connect

### To use NextCloud

1. Login to the NextCloud using the password with the OTP attached (from FreeOTP)
2. Enjoy it, share your files with anyone else~