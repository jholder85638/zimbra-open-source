
package generated.zcsclient.admin;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dedupeBlobsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dedupeBlobsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="volumeBlobsProgress" type="{urn:zimbraAdmin}volumeIdAndProgress" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="blobDigestsProgress" type="{urn:zimbraAdmin}volumeIdAndProgress" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="status" type="{urn:zimbraAdmin}dedupStatus" />
 *       &lt;attribute name="totalSize" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="totalCount" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dedupeBlobsResponse", propOrder = {
    "volumeBlobsProgress",
    "blobDigestsProgress"
})
public class testDedupeBlobsResponse {

    protected List<testVolumeIdAndProgress> volumeBlobsProgress;
    protected List<testVolumeIdAndProgress> blobDigestsProgress;
    @XmlAttribute(name = "status")
    protected testDedupStatus status;
    @XmlAttribute(name = "totalSize")
    protected Long totalSize;
    @XmlAttribute(name = "totalCount")
    protected Integer totalCount;

    /**
     * Gets the value of the volumeBlobsProgress property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the volumeBlobsProgress property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVolumeBlobsProgress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link testVolumeIdAndProgress }
     * 
     * 
     */
    public List<testVolumeIdAndProgress> getVolumeBlobsProgress() {
        if (volumeBlobsProgress == null) {
            volumeBlobsProgress = new ArrayList<testVolumeIdAndProgress>();
        }
        return this.volumeBlobsProgress;
    }

    /**
     * Gets the value of the blobDigestsProgress property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the blobDigestsProgress property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBlobDigestsProgress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link testVolumeIdAndProgress }
     * 
     * 
     */
    public List<testVolumeIdAndProgress> getBlobDigestsProgress() {
        if (blobDigestsProgress == null) {
            blobDigestsProgress = new ArrayList<testVolumeIdAndProgress>();
        }
        return this.blobDigestsProgress;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link testDedupStatus }
     *     
     */
    public testDedupStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link testDedupStatus }
     *     
     */
    public void setStatus(testDedupStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the totalSize property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTotalSize() {
        return totalSize;
    }

    /**
     * Sets the value of the totalSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTotalSize(Long value) {
        this.totalSize = value;
    }

    /**
     * Gets the value of the totalCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalCount() {
        return totalCount;
    }

    /**
     * Sets the value of the totalCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalCount(Integer value) {
        this.totalCount = value;
    }

}
