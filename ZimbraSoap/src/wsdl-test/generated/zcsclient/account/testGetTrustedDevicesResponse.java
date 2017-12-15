
package generated.zcsclient.account;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getTrustedDevicesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getTrustedDevicesResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="nOtherDevices" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="thisDeviceTrusted" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getTrustedDevicesResponse")
public class testGetTrustedDevicesResponse {

    @XmlAttribute(name = "nOtherDevices")
    protected Integer nOtherDevices;
    @XmlAttribute(name = "thisDeviceTrusted")
    protected Boolean thisDeviceTrusted;

    /**
     * Gets the value of the nOtherDevices property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNOtherDevices() {
        return nOtherDevices;
    }

    /**
     * Sets the value of the nOtherDevices property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNOtherDevices(Integer value) {
        this.nOtherDevices = value;
    }

    /**
     * Gets the value of the thisDeviceTrusted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isThisDeviceTrusted() {
        return thisDeviceTrusted;
    }

    /**
     * Sets the value of the thisDeviceTrusted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setThisDeviceTrusted(Boolean value) {
        this.thisDeviceTrusted = value;
    }

}
