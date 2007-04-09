/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jsch.internal.core;

import java.util.Hashtable;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;

import com.jcraft.jsch.*;

public class Utils{

  public static String loadPrivateKeys(JSch jsch, String current_pkeys){
    Preferences preferences=JSchCorePlugin.getPlugin().getPluginPreferences();
    String ssh_home=preferences.getString(IConstants.KEY_SSH2HOME);
    String pkeys=preferences.getString(IConstants.KEY_PRIVATEKEY);

    if(ssh_home.length()==0)
      ssh_home=PreferenceInitializer.SSH_HOME_DEFAULT;

    java.io.File file;
    String[] pkey=pkeys.split(","); //$NON-NLS-1$
    String[] _pkey=current_pkeys.split(","); //$NON-NLS-1$
    String result=""; //$NON-NLS-1$
    for(int i=0; i<pkey.length; i++){
      file=new java.io.File(pkey[i]);
      if(!file.isAbsolute()){
        file=new java.io.File(ssh_home, pkey[i]);
      }
      if(file.exists()){
        boolean notyet=true;
        for(int j=0; j<_pkey.length; j++){
          if(pkey[i].equals(_pkey[j])){
            notyet=false;
            break;
          }
        }
        try{
          if(notyet)
            jsch.addIdentity(file.getPath());
          if(result.length()==0){
            result=pkey[i];
          }
          else{
            result+=(","+pkey[i]); //$NON-NLS-1$
          }
        }
        catch(JSchException e){
          JSchCorePlugin.log(IStatus.ERROR,
              "An error occurred loading the SSH2 private keys", e); //$NON-NLS-1$
        }
      }
    }
    return result;
  }

  public static Session createSession(JSch jsch, String username,
      String hostname, int port) throws JSchException{
    Session session=jsch.getSession(username, hostname, port);
    setProxy(session);
    Hashtable config=new Hashtable();
    config.put("PreferredAuthentications", //$NON-NLS-1$ 
        "gssapi-with-mic,publickey,password,keyboard-interactive"); //$NON-NLS-1$ 
    session.setConfig(config);
    return session;
  }

  public static void setProxy(Session session){
    Proxy proxy=getProxyForHost(session.getHost(), IProxyData.SOCKS_PROXY_TYPE);
    if(proxy==null)
      proxy=getProxyForHost(session.getHost(), IProxyData. HTTPS_PROXY_TYPE);
    if(proxy!=null)
      session.setProxy(proxy);
  }

  private static int getPort(IProxyData data){
    int port=data.getPort();
    if(port==-1){
      if(data.getType().equals(IProxyData.HTTP_PROXY_TYPE))
        port=80;
      else if(data.getType().equals(IProxyData.HTTPS_PROXY_TYPE))
        port=443;
      else if(data.getType().equals(IProxyData.SOCKS_PROXY_TYPE))
        port=1080;
    }
    return port;
  }

  private static IProxyData getProxyData(String host, String type){
    IProxyService proxyService=JSchCorePlugin.getPlugin().getProxyService();
    if(proxyService==null)
      return null;
    IProxyData data=proxyService.getProxyDataForHost(host, type);
    if(data==null||data.getHost()==null||getJSchProxyType(data)==null)
      return null;
    return data;
  }

  private static String getJSchProxyType(IProxyData data){
    if(data.getType().equals(IProxyData.HTTPS_PROXY_TYPE))
      return IConstants.PROXY_TYPE_HTTP;
    if(data.getType().equals(IProxyData.SOCKS_PROXY_TYPE))
      return IConstants.PROXY_TYPE_SOCKS5;
    return null;
  }

  public static Proxy getProxyForHost(String host, String proxyType){
    IProxyService proxyService=JSchCorePlugin.getPlugin().getProxyService();
    if(proxyService==null)
      return null;
    boolean useProxy=proxyService.isProxiesEnabled();
    if(!useProxy)
      return null;
    Proxy proxy=null;
    IProxyData data=getProxyData(host, proxyType);
    if(data==null)
      return null;
    String _type=getJSchProxyType(data);
    if(_type==null)
      return null;
    String _host=data.getHost();
    int _port=getPort(data);

    boolean useAuth=data.isRequiresAuthentication();
    String _user=""; //$NON-NLS-1$
    String _pass=""; //$NON-NLS-1$

    // Retrieve username and password from keyring.
    if(useAuth){
      _user=data.getUserId();
      _pass=data.getPassword();
    }

    String proxyhost=_host+":"+_port; //$NON-NLS-1$
    if(_type.equals(IConstants.PROXY_TYPE_HTTP)){
      proxy=new ProxyHTTP(proxyhost);
      if(useAuth){
        ((ProxyHTTP)proxy).setUserPasswd(_user, _pass);
      }
    }
    else if(_type.equals(IConstants.PROXY_TYPE_SOCKS5)){
      proxy=new ProxySOCKS5(proxyhost);
      if(useAuth){
        ((ProxySOCKS5)proxy).setUserPasswd(_user, _pass);
      }
    }
    return proxy;
  }

}
