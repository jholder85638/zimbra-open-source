
package generated.zcsclient.zm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HeaderContext complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HeaderContext">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="authToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="session" type="{urn:zimbra}headerSessionInfo" minOccurs="0"/>
 *         &lt;element name="sessionId" type="{urn:zimbra}headerSessionInfo" minOccurs="0"/>
 *         &lt;element name="nosession" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="account" type="{urn:zimbra}headerAccountInfo" minOccurs="0"/>
 *         &lt;element name="change" type="{urn:zimbra}headerChangeInfo" minOccurs="0"/>
 *         &lt;element name="targetServer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userAgent" type="{urn:zimbra}headerUserAgentInfo" minOccurs="0"/>
 *         &lt;element name="authTokenControl" type="{urn:zimbra}authTokenControl" minOccurs="0"/>
 *         &lt;element name="format" type="{urn:zimbra}headerFormatInfo" minOccurs="0"/>
 *         &lt;element name="notify" type="{urn:zimbra}headerNotifyInfo" minOccurs="0"/>
 *         &lt;element name="nonotify" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="noqualify" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="via" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="soapId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="csrfToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/all>
 *       &lt;attribute name="hops" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HeaderContext", propOrder = {

})
public class testHeaderContext {

    protected String authToken;
    protected testHeaderSessionInfo session;
    protected testHeaderSessionInfo sessionId;
    protected String nosession;
    protected testHeaderAccountInfo account;
    protected testHeaderChangeInfo change;
    protected String targetServer;
    protected testHeaderUserAgentInfo userAgent;
    protected testAuthTokenControl authTokenControl;
    protected testHeaderFormatInfo format;
    protected testHeaderNotifyInfo notify;
    protected String nonotify;
    protected String noqualify;
    protected String via;
    protected String soapId;
    protected String csrfToken;
    @XmlAttribute(name = "hops")
    protected Integer hops;

    /**
     * Gets the value of the authToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Sets the value of the authToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthToken(String value) {
        this.authToken = value;
    }

    /**
     * Gets the value of the session property.
     * 
     * @return
     *     possible object is
     *     {@link testHeaderSessionInfo }
     *     
     */
    public testHeaderSessionInfo getSession() {
        return session;
    }

    /**
     * Sets the value of the session property.
     * 
     * @param value
     *     allowed object is
     *     {@link testHeaderSessionInfo }
     *     
     */
    public void setSession(testHeaderSessionInfo value) {
        this.session = value;
    }

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link testHeaderSessionInfo }
     *     
     */
    public testHeaderSessionInfo getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link testHeaderSessionInfo }
     *     
     */
    public void setSessionId(testHeaderSessionInfo value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the nosession property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNosession() {
        return nosession;
    }

    /**
     * Sets the value of the nosession property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNosession(String value) {
        this.nosession = value;
    }

    /**
     * Gets the value of the account property.
     * 
     * @return
     *     possible object is
     *     {@link testHeaderAccountInfo }
     *     
     */
    public testHeaderAccountInfo getAccount() {
        return account;
    }

    /**
     * Sets the value of the account property.
     * 
     * @param value
     *     allowed object is
     *     {@link testHeaderAccountInfo }
     *     
     */
    public void setAccount(testHeaderAccountInfo value) {
        this.account = value;
    }

    /**
     * Gets the value of the change property.
     * 
     * @return
     *     possible object is
     *     {@link testHeaderChangeInfo }
     *     
     */
    public testHeaderChangeInfo getChange() {
        return change;
    }

    /**
     * Sets the value of the change property.
     * 
     * @param value
     *     allowed object is
     *     {@link testHeaderChangeInfo }
     *     
     */
    public void setChange(testHeaderChangeInfo value) {
        this.change = value;
    }

    /**
     * Gets the value of the targetServer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetServer() {
        return targetServer;
    }

    /**
     * Sets the value of the targetServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetServer(String value) {
        this.targetServer = value;
    }

    /**
     * Gets the value of the userAgent property.
     * 
     * @return
     *     possible object is
     *     {@link testHeaderUserAgentInfo }
     *     
     */
    public testHeaderUserAgentInfo getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the value of the userAgent property.
     * 
     * @param value
     *     allowed object is
     *     {@link testHeaderUserAgentInfo }
     *     
     */
    public void setUserAgent(testHeaderUserAgentInfo value) {
        this.userAgent = value;
    }

    /**
     * Gets the value of the authTokenControl property.
     * 
     * @return
     *     possible object is
     *     {@link testAuthTokenControl }
     *     
     */
    public testAuthTokenControl getAuthTokenControl() {
        return authTokenControl;
    }

    /**
     * Sets the value of the authTokenControl property.
     * 
     * @param value
     *     allowed object is
     *     {@link testAuthTokenControl }
     *     
     */
    public void setAuthTokenControl(testAuthTokenControl value) {
        this.authTokenControl = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link testHeaderFormatInfo }
     *     
     */
    public testHeaderFormatInfo getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link testHeaderFormatInfo }
     *     
     */
    public void setFormat(testHeaderFormatInfo value) {
        this.format = value;
    }

    /**
     * Gets the value of the notify property.
     * 
     * @return
     *     possible object is
     *     {@link testHeaderNotifyInfo }
     *     
     */
    public testHeaderNotifyInfo getNotify() {
        return notify;
    }

    /**
     * Sets the value of the notify property.
     * 
     * @param value
     *     allowed object is
     *     {@link testHeaderNotifyInfo }
     *     
     */
    public void setNotify(testHeaderNotifyInfo value) {
        this.notify = value;
    }

    /**
     * Gets the value of the nonotify property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNonotify() {
        return nonotify;
    }

    /**
     * Sets the value of the nonotify property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNonotify(String value) {
        this.nonotify = value;
    }

    /**
     * Gets the value of the noqualify property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoqualify() {
        return noqualify;
    }

    /**
     * Sets the value of the noqualify property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoqualify(String value) {
        this.noqualify = value;
    }

    /**
     * Gets the value of the via property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVia() {
        return via;
    }

    /**
     * Sets the value of the via property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVia(String value) {
        this.via = value;
    }

    /**
     * Gets the value of the soapId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSoapId() {
        return soapId;
    }

    /**
     * Sets the value of the soapId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSoapId(String value) {
        this.soapId = value;
    }

    /**
     * Gets the value of the csrfToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCsrfToken() {
        return csrfToken;
    }

    /**
     * Sets the value of the csrfToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCsrfToken(String value) {
        this.csrfToken = value;
    }

    /**
     * Gets the value of the hops property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHops() {
        return hops;
    }

    /**
     * Sets the value of the hops property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHops(Integer value) {
        this.hops = value;
    }

}
