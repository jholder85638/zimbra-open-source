
package generated.zcsclient.mail;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nestedRule complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nestedRule">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filterTests" type="{urn:zimbraMail}filterTests"/>
 *         &lt;element name="filterActions" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;choice maxOccurs="unbounded" minOccurs="0">
 *                     &lt;element name="actionKeep" type="{urn:zimbraMail}keepAction"/>
 *                     &lt;element name="actionDiscard" type="{urn:zimbraMail}discardAction"/>
 *                     &lt;element name="actionFileInto" type="{urn:zimbraMail}fileIntoAction"/>
 *                     &lt;element name="actionFlag" type="{urn:zimbraMail}flagAction"/>
 *                     &lt;element name="actionTag" type="{urn:zimbraMail}tagAction"/>
 *                     &lt;element name="actionRedirect" type="{urn:zimbraMail}redirectAction"/>
 *                     &lt;element name="actionReply" type="{urn:zimbraMail}replyAction"/>
 *                     &lt;element name="actionNotify" type="{urn:zimbraMail}notifyAction"/>
 *                     &lt;element name="actionStop" type="{urn:zimbraMail}stopAction"/>
 *                   &lt;/choice>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="nestedRule" type="{urn:zimbraMail}nestedRule" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nestedRule", propOrder = {
    "filterTests",
    "filterActions",
    "nestedRule"
})
public class testNestedRule {

    @XmlElement(required = true)
    protected testFilterTests filterTests;
    protected testNestedRule.FilterActions filterActions;
    protected testNestedRule nestedRule;

    /**
     * Gets the value of the filterTests property.
     * 
     * @return
     *     possible object is
     *     {@link testFilterTests }
     *     
     */
    public testFilterTests getFilterTests() {
        return filterTests;
    }

    /**
     * Sets the value of the filterTests property.
     * 
     * @param value
     *     allowed object is
     *     {@link testFilterTests }
     *     
     */
    public void setFilterTests(testFilterTests value) {
        this.filterTests = value;
    }

    /**
     * Gets the value of the filterActions property.
     * 
     * @return
     *     possible object is
     *     {@link testNestedRule.FilterActions }
     *     
     */
    public testNestedRule.FilterActions getFilterActions() {
        return filterActions;
    }

    /**
     * Sets the value of the filterActions property.
     * 
     * @param value
     *     allowed object is
     *     {@link testNestedRule.FilterActions }
     *     
     */
    public void setFilterActions(testNestedRule.FilterActions value) {
        this.filterActions = value;
    }

    /**
     * Gets the value of the nestedRule property.
     * 
     * @return
     *     possible object is
     *     {@link testNestedRule }
     *     
     */
    public testNestedRule getNestedRule() {
        return nestedRule;
    }

    /**
     * Sets the value of the nestedRule property.
     * 
     * @param value
     *     allowed object is
     *     {@link testNestedRule }
     *     
     */
    public void setNestedRule(testNestedRule value) {
        this.nestedRule = value;
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
     *         &lt;choice maxOccurs="unbounded" minOccurs="0">
     *           &lt;element name="actionKeep" type="{urn:zimbraMail}keepAction"/>
     *           &lt;element name="actionDiscard" type="{urn:zimbraMail}discardAction"/>
     *           &lt;element name="actionFileInto" type="{urn:zimbraMail}fileIntoAction"/>
     *           &lt;element name="actionFlag" type="{urn:zimbraMail}flagAction"/>
     *           &lt;element name="actionTag" type="{urn:zimbraMail}tagAction"/>
     *           &lt;element name="actionRedirect" type="{urn:zimbraMail}redirectAction"/>
     *           &lt;element name="actionReply" type="{urn:zimbraMail}replyAction"/>
     *           &lt;element name="actionNotify" type="{urn:zimbraMail}notifyAction"/>
     *           &lt;element name="actionStop" type="{urn:zimbraMail}stopAction"/>
     *         &lt;/choice>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "actionKeepOrActionDiscardOrActionFileInto"
    })
    public static class FilterActions {

        @XmlElements({
            @XmlElement(name = "actionStop", type = testStopAction.class),
            @XmlElement(name = "actionFlag", type = testFlagAction.class),
            @XmlElement(name = "actionFileInto", type = testFileIntoAction.class),
            @XmlElement(name = "actionDiscard", type = testDiscardAction.class),
            @XmlElement(name = "actionKeep", type = testKeepAction.class),
            @XmlElement(name = "actionNotify", type = testNotifyAction.class),
            @XmlElement(name = "actionReply", type = testReplyAction.class),
            @XmlElement(name = "actionRedirect", type = testRedirectAction.class),
            @XmlElement(name = "actionTag", type = testTagAction.class)
        })
        protected List<testFilterAction> actionKeepOrActionDiscardOrActionFileInto;

        /**
         * Gets the value of the actionKeepOrActionDiscardOrActionFileInto property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the actionKeepOrActionDiscardOrActionFileInto property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getActionKeepOrActionDiscardOrActionFileInto().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link testStopAction }
         * {@link testFlagAction }
         * {@link testFileIntoAction }
         * {@link testDiscardAction }
         * {@link testKeepAction }
         * {@link testNotifyAction }
         * {@link testReplyAction }
         * {@link testRedirectAction }
         * {@link testTagAction }
         * 
         * 
         */
        public List<testFilterAction> getActionKeepOrActionDiscardOrActionFileInto() {
            if (actionKeepOrActionDiscardOrActionFileInto == null) {
                actionKeepOrActionDiscardOrActionFileInto = new ArrayList<testFilterAction>();
            }
            return this.actionKeepOrActionDiscardOrActionFileInto;
        }

    }

}
