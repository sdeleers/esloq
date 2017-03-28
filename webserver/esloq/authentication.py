from esloq.models import EsloqUser
from rest_framework import authentication
from rest_framework import exceptions
from cryptography.exceptions import InvalidSignature
import jwt
from cryptography.x509 import load_pem_x509_certificate
from cryptography.hazmat.backends import default_backend
import urllib.request

class FirebaseAuthentication(authentication.BaseAuthentication):
    """
    Returns the authenticated user if the user provided a valid Firebase 
    authentication token in the Authorization header. The header must look like this:
    Authorization: Bearer <firebase_token>
    """
    def authenticate(self, request):
        auth_header = request.META.get('HTTP_AUTHORIZATION') # get the username request header

        # remove these 2 lines below
        # user = EsloqUser.objects.get(id=1) # get the user
        # return (user, None)

        if not auth_header: # no token passed in request headers
            return None # authentication did not succeed

        try:
            token = auth_header.split("Bearer ")[1]
            idinfo = _verifyidtoken(token)
        except IndexError as e:
            return None # authentication did not succeed
        except InvalidSignature as e:
            return None

        try:
            firebase_id = idinfo['sub']
            user = EsloqUser.objects.get(firebase_id=firebase_id) # get the user
        except EsloqUser.DoesNotExist:
            return None
            # raise exceptions.AuthenticationFailed('No such user') # raise exception if user does not exist 

        return (user, None) # authentication successful

def _verifyidtoken(token):
    # Open file containing Firebase's certificates
    with open('esloq/firebase_certs.txt', 'r') as f:
        data = f.read()
    certs = eval(data)
    with open('esloq/tests/cert.pem', 'r') as f:
        test_cert = f.read()
    certs["0"] = test_cert
    jwt_header = jwt.get_unverified_header(token)

    # Try to extract certificate, if it is not present, download the new certificates.
    try:
        certs[jwt_header["kid"]]
    except KeyError:
        url = "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
        with urllib.request.urlopen(url) as response:
               html = response.read()
        with open('esloq/firebase_certs.txt', 'wb') as f:
            f.write(html)
        certs = eval(html)

    certificate_text = bytes(certs[jwt_header["kid"]], "ASCII")
    public_key = load_pem_x509_certificate(certificate_text, default_backend()).public_key()

    try:
        verified_jwt = jwt.decode(token, public_key, audience='decoded-totem-95010', issuer='https://securetoken.google.com/decoded-totem-95010',  algorithm='RS256', leeway=10)
        return verified_jwt
    except:
        raise InvalidSignature("Invalid authorization token.")
