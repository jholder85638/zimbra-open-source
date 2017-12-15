
package generated.zcsclient.account;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for appSpecificPasswordData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="appSpecificPasswordData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="appName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="created" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="lastUsed" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "appSpecificPasswordData")
public class testAppSpecificPasswordData {

    @XmlAttribute(name = "appName")
    protected String appName;
    @XmlAttribute(name = "created")
    protected Long created;
    @XmlAttribute(name = "lastUsed")
    protected Long lastUsed;

    /**
     * Gets the value of the appName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets the value of the appName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppName(String value) {
        this.appName = value;
    }

    /**
     * Gets the value of the created property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCreated(Long value) {
        this.created = value;
    }

    /**
     * Gets the value of the lastUsed property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLastUsed() {
        return lastUsed;
    }

    /**
     * Sets the value of the lastUsed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLastUsed(Long value) {
        this.lastUsed = value;
    }

}
