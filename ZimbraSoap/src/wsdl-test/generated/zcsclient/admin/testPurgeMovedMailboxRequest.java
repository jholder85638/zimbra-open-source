
package generated.zcsclient.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for purgeMovedMailboxRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="purgeMovedMailboxRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mbox" type="{urn:zimbraAdmin}name"/>
 *       &lt;/sequence>
 *       &lt;attribute name="forceDeleteBlobs" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "purgeMovedMailboxRequest", propOrder = {
    "mbox"
})
public class testPurgeMovedMailboxRequest {

    @XmlElement(required = true)
    protected testName mbox;
    @XmlAttribute(name = "forceDeleteBlobs")
    protected Boolean forceDeleteBlobs;

    /**
     * Gets the value of the mbox property.
     * 
     * @return
     *     possible object is
     *     {@link testName }
     *     
     */
    public testName getMbox() {
        return mbox;
    }

    /**
     * Sets the value of the mbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link testName }
     *     
     */
    public void setMbox(testName value) {
        this.mbox = value;
    }

    /**
     * Gets the value of the forceDeleteBlobs property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForceDeleteBlobs() {
        return forceDeleteBlobs;
    }

    /**
     * Sets the value of the forceDeleteBlobs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForceDeleteBlobs(Boolean value) {
        this.forceDeleteBlobs = value;
    }

}
