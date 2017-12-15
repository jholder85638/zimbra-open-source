
package generated.zcsclient.account;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAppSpecificPasswordsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getAppSpecificPasswordsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="appSpecificPasswords" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="passwordData" type="{urn:zimbraAccount}appSpecificPasswordData" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="unusedCodeGenHelper" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="maxAppPasswords" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAppSpecificPasswordsResponse", propOrder = {

})
public class testGetAppSpecificPasswordsResponse {

    protected testGetAppSpecificPasswordsResponse.AppSpecificPasswords appSpecificPasswords;
    protected Integer maxAppPasswords;

    /**
     * Gets the value of the appSpecificPasswords property.
     * 
     * @return
     *     possible object is
     *     {@link testGetAppSpecificPasswordsResponse.AppSpecificPasswords }
     *     
     */
    public testGetAppSpecificPasswordsResponse.AppSpecificPasswords getAppSpecificPasswords() {
        return appSpecificPasswords;
    }

    /**
     * Sets the value of the appSpecificPasswords property.
     * 
     * @param value
     *     allowed object is
     *     {@link testGetAppSpecificPasswordsResponse.AppSpecificPasswords }
     *     
     */
    public void setAppSpecificPasswords(testGetAppSpecificPasswordsResponse.AppSpecificPasswords value) {
        this.appSpecificPasswords = value;
    }

    /**
     * Gets the value of the maxAppPasswords property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxAppPasswords() {
        return maxAppPasswords;
    }

    /**
     * Sets the value of the maxAppPasswords property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxAppPasswords(Integer value) {
        this.maxAppPasswords = value;
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
     *         &lt;element name="passwordData" type="{urn:zimbraAccount}appSpecificPasswordData" maxOccurs="unbounded" minOccurs="0"/>
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
        "passwordData"
    })
    public static class AppSpecificPasswords {

        protected List<testAppSpecificPasswordData> passwordData;
        @XmlAttribute(name = "unusedCodeGenHelper")
        protected String unusedCodeGenHelper;

        /**
         * Gets the value of the passwordData property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the passwordData property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPasswordData().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link testAppSpecificPasswordData }
         * 
         * 
         */
        public List<testAppSpecificPasswordData> getPasswordData() {
            if (passwordData == null) {
                passwordData = new ArrayList<testAppSpecificPasswordData>();
            }
            return this.passwordData;
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
