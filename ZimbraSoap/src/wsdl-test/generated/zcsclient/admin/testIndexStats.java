
package generated.zcsclient.admin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for indexStats complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="indexStats">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *       &lt;/all>
 *       &lt;attribute name="maxDocs" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="deletedDocs" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "indexStats", propOrder = {

})
public class testIndexStats {

    @XmlAttribute(name = "maxDocs", required = true)
    protected int maxDocs;
    @XmlAttribute(name = "deletedDocs", required = true)
    protected int deletedDocs;

    /**
     * Gets the value of the maxDocs property.
     * 
     */
    public int getMaxDocs() {
        return maxDocs;
    }

    /**
     * Sets the value of the maxDocs property.
     * 
     */
    public void setMaxDocs(int value) {
        this.maxDocs = value;
    }

    /**
     * Gets the value of the deletedDocs property.
     * 
     */
    public int getDeletedDocs() {
        return deletedDocs;
    }

    /**
     * Sets the value of the deletedDocs property.
     * 
     */
    public void setDeletedDocs(int value) {
        this.deletedDocs = value;
    }

}
