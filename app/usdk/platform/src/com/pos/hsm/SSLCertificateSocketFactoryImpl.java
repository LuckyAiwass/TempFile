
package com.pos.hsm;

//import com.android.org.bouncycastle.util.encoders.Base64;
import android.util.Base64;
//import com.pos.hsm.PKCS11Wrapper;
import android.device.HwSecurityManager;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
//https://stackoverflow.com/questions/29916962/javax-net-ssl-sslhandshakeexception-javax-net-ssl-sslprotocolexception-ssl-han
public class SSLCertificateSocketFactoryImpl extends SSLSocketFactory {
    private final SSLSocketFactory delegate;
    private SSLContext sslContext;
    private HwSecurityManager pkcs11Wrapper;
    public SSLCertificateSocketFactoryImpl() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        //this.delegate = HttpsURLConnection.getDefaultSSLSocketFactory();
        sslContext = SSLContext.getInstance("SSL");
        pkcs11Wrapper = new HwSecurityManager();
        final String keystoreAlias = System.getProperty("javax.net.ssl.certAlias");
        TrustManager trustManager = new X509TrustManager() {
            
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                logwarn("alias =========getAcceptedIssuers===== ");
                return new X509Certificate[0];      
            }      
      
            @Override      
            public void checkClientTrusted(      
                    java.security.cert.X509Certificate[] chain, String authType)      
                    throws java.security.cert.CertificateException {
                logwarn("=====================checkClientTrusted===================");
                if(chain != null)logwarn("length = " + chain.length);
            }
      
            @Override      
            public void checkServerTrusted(      
                    java.security.cert.X509Certificate[] chain, String authType)      
                    throws java.security.cert.CertificateException {      
                logwarn("=====================checkServerTrusted===================");
                if(chain != null)logwarn("length = " + chain.length);
                try {
                    CertificateFactory factory = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = null;
                    X509Certificate ca;
                    ByteArrayInputStream bais;
                    if(chain != null && chain.length > 0){
                        bais = new ByteArrayInputStream(chain[0].getEncoded());
                        cert = (X509Certificate) factory.generateCertificate(bais);
                        bais.close();
                    } else {
                        throw new CertificateException("There is no certificate");
                    }
                    byte[] trust =  pkcs11Wrapper.getCertificate(0,"trust01",HwSecurityManager.CERT_FORMAT_PEM);
                    logwarn("trust cert = " + new String(trust));
                    bais = new ByteArrayInputStream(trust);
                    CertificateFactory myCertificateFactory;
                    ca = (X509Certificate)factory.generateCertificate(bais);
                    bais.close();
                    cert.verify(ca.getPublicKey());
                }catch (Exception e){
                    logwarn("TrustManager checkServerTrusted failed!");
                    e.printStackTrace();
                    throw new CertificateException(e);
                }
            }      
        };
        X509KeyManager keyManager = new X509KeyManager() {
            @Override
            public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {

                logwarn("=====================chooseClientAlias===================");
                if(keyType != null && keyType.length > 0){
                    for(int i = 0; i < keyType.length; i++){
                        logwarn("keyType["+i+"]=" + keyType[i]);
                    }
                }
                if(issuers != null && issuers.length > 0){
                    for(int i = 0; i < issuers.length; i++){
                        logwarn("issuers["+i+"]=" + issuers[i].getName());
                    }
                }
                return "pk2048";
            }

            @Override
            public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
                logwarn("=====================chooseServerAlias===================");
                logwarn("keyType================" + keyType);
                if(issuers != null && issuers.length > 0){
                    for(int i = 0; i < issuers.length; i++){
                        logwarn("issuers["+i+"]=" + issuers[i].getName());
                    }
                }
                return "trust01";
            }

            @Override
            public X509Certificate[] getCertificateChain(String alias) {
                logwarn("=====================getCertificateChain==================keystoreAlias="+ keystoreAlias);
                logwarn("alias ============== " + alias);
                byte[] publickey = pkcs11Wrapper.getCertificate(0,"client2048",HwSecurityManager.CERT_FORMAT_PEM);
                InputStream ceris = new ByteArrayInputStream(publickey);
                CertificateFactory myCertificateFactory;
                try {
                    myCertificateFactory = CertificateFactory.getInstance("X.509");
                    X509Certificate cer = (X509Certificate)myCertificateFactory.generateCertificate(ceris);
                    ceris.close();
                    return new X509Certificate[]{cer};
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return new X509Certificate[0];
            }

            @Override
            public String[] getClientAliases(String keyType, Principal[] issuers) {
                logwarn("=====================getClientAliases===================");
                logwarn("keyType================" + keyType);
                if(issuers != null && issuers.length > 0){
                    for(int i = 0; i < issuers.length; i++){
                        logwarn("issuers["+i+"]=" + issuers[i].getName());
                    }
                }
                return new String[0];
            }

            @Override
            public String[] getServerAliases(String keyType, Principal[] issuers) {
                logwarn("=====================getServerAliases===================");
                logwarn("keyType================" + keyType);
                if(issuers != null && issuers.length > 0){
                    for(int i = 0; i < issuers.length; i++){
                        logwarn("issuers["+i+"]=" + issuers[i].getName());
                    }
                }
                return new String[0];
            }

            @Override
            public PrivateKey getPrivateKey(String alias) {
                logwarn("=====================getPrivateKey===================");
                logwarn("alias ============== " + alias);
                byte[] keyData = pkcs11Wrapper.getCertificate(0,alias,HwSecurityManager.CERT_FORMAT_PEM);
                String keyString = new String(keyData);
                keyString = keyString.replaceAll("\n", "");
                Pattern pattern =Pattern.compile("-----BEGIN RSA PRIVATE KEY-----(.*?)-----END RSA PRIVATE KEY-----");
                Matcher matcher=pattern.matcher(keyString);
                while (matcher.find()) {
                    keyData = matcher.group(1).getBytes();
                }
                try {
                    PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decode(keyData,Base64.DEFAULT));
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA","BC");
                    PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
                    return privateK;
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        };
        sslContext.init(new KeyManager[]{keyManager}, new TrustManager[]{trustManager},null);
        this.delegate = sslContext.getSocketFactory();
    }

    public SSLCertificateSocketFactoryImpl(SSLSocketFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    private Socket makeSocketSafe(Socket socket) {
        if (socket instanceof SSLSocket) {
            socket = new NoSSLv3SSLSocket((SSLSocket) socket);
        }
        return socket;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        return makeSocketSafe(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return makeSocketSafe(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        return makeSocketSafe(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return makeSocketSafe(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException {
        return makeSocketSafe(delegate.createSocket(address, port, localAddress, localPort));
    }

    private class NoSSLv3SSLSocket extends DelegateSSLSocket {

        private NoSSLv3SSLSocket(SSLSocket delegate) {
            super(delegate);

        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
           /* if (protocols != null && protocols.length == 1 && "SSLv3".equals(protocols[0])) {

                List<String> enabledProtocols = new ArrayList<String>(Arrays.asList(delegate
                        .getEnabledProtocols()));
                for (String cipherSuite : enabledProtocols) {
                    logwarn("protocols ====setEnabledProtocols========== " + cipherSuite);
                  }
                if (enabledProtocols.size() > 1) {
                    enabledProtocols.remove("SSLv3");
                    System.out.println("Removed SSLv3 from enabled protocols");
                } else {
                    System.out.println("SSL stuck with protocol available for "
                            + String.valueOf(enabledProtocols));
                }
                protocols = enabledProtocols.toArray(new String[enabledProtocols.size()]);
            }*/

            super.setEnabledProtocols(protocols);
        }
    }
    void logwarn(String msg) {
        Log.d("SSLCertificateSocketFactoryImpl", msg);
    }
    public class DelegateSSLSocket extends SSLSocket {

        protected final SSLSocket delegate;

        DelegateSSLSocket(SSLSocket delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public String[] getEnabledCipherSuites() {
            String [] CipherSuites = delegate.getEnabledCipherSuites();
            /*for (String cipherSuite : CipherSuites) {
                logwarn("CipherSuites ====getEnabledCipherSuites========== " + cipherSuite);
            }*/
            return CipherSuites;
        }

        @Override
        public void setEnabledCipherSuites(String[] suites) {
            /*for (String cipherSuite : suites) {
                logwarn("CipherSuites ====setEnabledCipherSuites========== " + cipherSuite);
            }*/
            delegate.setEnabledCipherSuites(suites);
        }

        @Override
        public String[] getSupportedProtocols() {
            String [] Protocols = delegate.getSupportedProtocols();
            /*for (String cipherSuite : Protocols) {
                logwarn("Protocols ====getSupportedProtocols========== " + cipherSuite);
            }*/
            return Protocols;
        }

        @Override
        public String[] getEnabledProtocols() {
            String [] Protocols = delegate.getEnabledProtocols();
            /*for (String cipherSuite : Protocols) {
                logwarn("Protocols ====getEnabledProtocols========== " + cipherSuite);
            }*/
            return Protocols;
        }

        @Override
        public void setEnabledProtocols(String[] protocols) {
            /*for (String cipherSuite : protocols) {
                logwarn("Protocols ====setEnabledProtocols========== " + cipherSuite);
            }*/
            delegate.setEnabledProtocols(protocols);
        }

        @Override
        public SSLSession getSession() {
            return delegate.getSession();
        }

        @Override
        public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
            delegate.addHandshakeCompletedListener(listener);
        }

        @Override
        public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
            delegate.removeHandshakeCompletedListener(listener);
        }

        @Override
        public void startHandshake() throws IOException {
            delegate.startHandshake();
        }

        @Override
        public void setUseClientMode(boolean mode) {
            delegate.setUseClientMode(mode);
        }

        @Override
        public boolean getUseClientMode() {
            return delegate.getUseClientMode();
        }

        @Override
        public void setNeedClientAuth(boolean need) {
            delegate.setNeedClientAuth(need);
        }

        @Override
        public void setWantClientAuth(boolean want) {
            delegate.setWantClientAuth(want);
        }

        @Override
        public boolean getNeedClientAuth() {
            return delegate.getNeedClientAuth();
        }

        @Override
        public boolean getWantClientAuth() {
            return delegate.getWantClientAuth();
        }

        @Override
        public void setEnableSessionCreation(boolean flag) {
            delegate.setEnableSessionCreation(flag);
        }

        @Override
        public boolean getEnableSessionCreation() {
            return delegate.getEnableSessionCreation();
        }

        @Override
        public void bind(SocketAddress localAddr) throws IOException {
            delegate.bind(localAddr);
        }

        @Override
        public synchronized void close() throws IOException {
            delegate.close();
        }

        @Override
        public void connect(SocketAddress remoteAddr) throws IOException {
            delegate.connect(remoteAddr);
        }

        @Override
        public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
            delegate.connect(remoteAddr, timeout);
        }

        @Override
        public SocketChannel getChannel() {
            return delegate.getChannel();
        }

        @Override
        public InetAddress getInetAddress() {
            return delegate.getInetAddress();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public boolean getKeepAlive() throws SocketException {
            return delegate.getKeepAlive();
        }

        @Override
        public InetAddress getLocalAddress() {
            return delegate.getLocalAddress();
        }

        @Override
        public int getLocalPort() {
            return delegate.getLocalPort();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return delegate.getLocalSocketAddress();
        }

        @Override
        public boolean getOOBInline() throws SocketException {
            return delegate.getOOBInline();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return delegate.getOutputStream();
        }

        @Override
        public int getPort() {
            return delegate.getPort();
        }

        @Override
        public synchronized int getReceiveBufferSize() throws SocketException {
            return delegate.getReceiveBufferSize();
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return delegate.getRemoteSocketAddress();
        }

        @Override
        public boolean getReuseAddress() throws SocketException {
            return delegate.getReuseAddress();
        }

        @Override
        public synchronized int getSendBufferSize() throws SocketException {
            return delegate.getSendBufferSize();
        }

        @Override
        public int getSoLinger() throws SocketException {
            return delegate.getSoLinger();
        }

        @Override
        public synchronized int getSoTimeout() throws SocketException {
            return delegate.getSoTimeout();
        }

        @Override
        public boolean getTcpNoDelay() throws SocketException {
            return delegate.getTcpNoDelay();
        }

        @Override
        public int getTrafficClass() throws SocketException {
            return delegate.getTrafficClass();
        }

        @Override
        public boolean isBound() {
            return delegate.isBound();
        }

        @Override
        public boolean isClosed() {
            return delegate.isClosed();
        }

        @Override
        public boolean isConnected() {
            return delegate.isConnected();
        }

        @Override
        public boolean isInputShutdown() {
            return delegate.isInputShutdown();
        }

        @Override
        public boolean isOutputShutdown() {
            return delegate.isOutputShutdown();
        }

        @Override
        public void sendUrgentData(int value) throws IOException {
            delegate.sendUrgentData(value);
        }

        @Override
        public void setKeepAlive(boolean keepAlive) throws SocketException {
            delegate.setKeepAlive(keepAlive);
        }

        @Override
        public void setOOBInline(boolean oobinline) throws SocketException {
            delegate.setOOBInline(oobinline);
        }

        @Override
        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
            delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
        }

        @Override
        public synchronized void setReceiveBufferSize(int size) throws SocketException {
            delegate.setReceiveBufferSize(size);
        }

        @Override
        public void setReuseAddress(boolean reuse) throws SocketException {
            delegate.setReuseAddress(reuse);
        }

        @Override
        public synchronized void setSendBufferSize(int size) throws SocketException {
            delegate.setSendBufferSize(size);
        }

        @Override
        public void setSoLinger(boolean on, int timeout) throws SocketException {
            delegate.setSoLinger(on, timeout);
        }

        @Override
        public synchronized void setSoTimeout(int timeout) throws SocketException {
            delegate.setSoTimeout(timeout);
        }

        @Override
        public void setTcpNoDelay(boolean on) throws SocketException {
            delegate.setTcpNoDelay(on);
        }

        @Override
        public void setTrafficClass(int value) throws SocketException {
            delegate.setTrafficClass(value);
        }

        @Override
        public void shutdownInput() throws IOException {
            delegate.shutdownInput();
        }

        @Override
        public void shutdownOutput() throws IOException {
            delegate.shutdownOutput();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }
    }
}
