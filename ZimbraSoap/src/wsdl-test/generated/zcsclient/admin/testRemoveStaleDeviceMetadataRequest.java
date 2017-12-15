
package generated.zcsclient.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for removeStaleDeviceMetadataRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="removeStaleDeviceMetadataRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="lastUsedDateOlderThan" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "removeStaleDeviceMetadataRequest")
public class testRemoveStaleDeviceMetadataRequest {

    @XmlAttribute(name = "lastUsedDateOlderThan", required = true)
    protected int lastUsedDateOlderThan;

    /**
     * Gets the value of the lastUsedDateOlderThan property.
     * 
     */
    public int getLastUsedDateOlderThan() {
        return lastUsedDateOlderThan;
    }

    /**
     * Sets the value of the lastUsedDateOlderThan property.
     * 
     */
    public void setLastUsedDateOlderThan(int value) {
        this.lastUsedDateOlderThan = value;
    }

}
