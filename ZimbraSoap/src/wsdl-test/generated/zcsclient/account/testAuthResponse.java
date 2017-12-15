
package generated.zcsclient.account;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for authResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="authResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="authToken" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lifetime" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="trustLifetime" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="session" type="{urn:zimbraAccount}session" minOccurs="0"/>
 *         &lt;element name="refer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="skin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="csrfToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="deviceId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="trustedToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="prefs" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="pref" type="{urn:zimbraAccount}pref" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="unusedCodeGenHelper" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="attrs" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="attr" type="{urn:zimbraAccount}attr" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="unusedCodeGenHelper" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "authResponse", propOrder = {

})
public class testAuthResponse {

    @XmlElement(required = true)
    protected String authToken;
    protected long lifetime;
    protected Long trustLifetime;
    protected testSession session;
    protected String refer;
    protected String skin;
    protected String csrfToken;
    protected String deviceId;
    protected String trustedToken;
    protected testAuthResponse.Prefs prefs;
    protected testAuthResponse.Attrs attrs;

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
     * Gets the value of the lifetime property.
     * 
     */
    public long getLifetime() {
        return lifetime;
    }

    /**
     * Sets the value of the lifetime property.
     * 
     */
    public void setLifetime(long value) {
        this.lifetime = value;
    }

    /**
     * Gets the value of the trustLifetime property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTrustLifetime() {
        return trustLifetime;
    }

    /**
     * Sets the value of the trustLifetime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTrustLifetime(Long value) {
        this.trustLifetime = value;
    }

    /**
     * Gets the value of the session property.
     * 
     * @return
     *     possible object is
     *     {@link testSession }
     *     
     */
    public testSession getSession() {
        return session;
    }

    /**
     * Sets the value of the session property.
     * 
     * @param value
     *     allowed object is
     *     {@link testSession }
     *     
     */
    public void setSession(testSession value) {
        this.session = value;
    }

    /**
     * Gets the value of the refer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefer() {
        return refer;
    }

    /**
     * Sets the value of the refer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefer(String value) {
        this.refer = value;
    }

    /**
     * Gets the value of the skin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSkin() {
        return skin;
    }

    /**
     * Sets the value of the skin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSkin(String value) {
        this.skin = value;
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
     * Gets the value of the deviceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the value of the deviceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceId(String value) {
        this.deviceId = value;
    }

    /**
     * Gets the value of the trustedToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrustedToken() {
        return trustedToken;
    }

    /**
     * Sets the value of the trustedToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrustedToken(String value) {
        this.trustedToken = value;
    }

    /**
     * Gets the value of the prefs property.
     * 
     * @return
     *     possible object is
     *     {@link testAuthResponse.Prefs }
     *     
     */
    public testAuthResponse.Prefs getPrefs() {
        return prefs;
    }

    /**
     * Sets the value of the prefs property.
     * 
     * @param value
     *     allowed object is
     *     {@link testAuthResponse.Prefs }
     *     
     */
    public void setPrefs(testAuthResponse.Prefs value) {
        this.prefs = value;
    }

    /**
     * Gets the value of the attrs property.
     * 
     * @return
     *     possible object is
     *     {@link testAuthResponse.Attrs }
     *     
     */
    public testAuthResponse.Attrs getAttrs() {
        return attrs;
    }

    /**
     * Sets the value of the attrs property.
     * 
     * @param value
     *     allowed object is
     *     {@link testAuthResponse.Attrs }
     *     
     */
    public void setAttrs(testAuthResponse.Attrs value) {
        this.attrs = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="attr" type="{urn:zimbraAccount}attr" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="unusedCodeGenHelper" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "attr"
    })
    public static class Attrs {

        protected List<testAttr> attr;
        @XmlAttribute(name = "unusedCodeGenHelper")
        protected String unusedCodeGenHelper;

        /**
         * Gets the value of the attr property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the attr property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAttr().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link testAttr }
         * 
         * 
         */
        public List<testAttr> getAttr() {
            if (attr == null) {
                attr = new ArrayList<testAttr>();
            }
            return this.attr;
        }

        /**
         * Gets the value of the unusedCodeGenHelper property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUnusedCodeGenHelper() {
            return unusedCodeGenHelper;
        }

        /**
         * Sets the value of the unusedCodeGenHelper property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUnusedCodeGenHelper(String value) {
            this.unusedCodeGenHelper = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="pref" type="{urn:zimbraAccount}pref" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="unusedCodeGenHelper" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "pref"
    })
    public static class Prefs {

        protected List<testPref> pref;
        @XmlAttribute(name = "unusedCodeGenHelper")
        protected String unusedCodeGenHelper;

        /**
         * Gets the value of the pref property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the pref property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPref().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link testPref }
         * 
         * 
         */
        public List<testPref> getPref() {
            if (pref == null) {
                pref = new ArrayList<testPref>();
            }
            return this.pref;
        }

        /**
         * Gets the value of the unusedCodeGenHelper property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUnusedCodeGenHelper() {
            return unusedCodeGenHelper;
        }

        /**
         * Sets the value of the unusedCodeGenHelper property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUnusedCodeGenHelper(String value) {
            this.unusedCodeGenHelper = value;
        }

    }

}
