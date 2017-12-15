
package generated.zcsclient.mail;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for syncFolder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="syncFolder">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:zimbraMail}folder">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="tag" type="{urn:zimbraMail}tagIdsAttr"/>
 *           &lt;element name="c" type="{urn:zimbraMail}convIdsAttr"/>
 *           &lt;element name="chat" type="{urn:zimbraMail}chatIdsAttr"/>
 *           &lt;element name="m" type="{urn:zimbraMail}msgIdsAttr"/>
 *           &lt;element name="cn" type="{urn:zimbraMail}contactIdsAttr"/>
 *           &lt;element name="appt" type="{urn:zimbraMail}apptIdsAttr"/>
 *           &lt;element name="task" type="{urn:zimbraMail}taskIdsAttr"/>
 *           &lt;element name="notes" type="{urn:zimbraMail}noteIdsAttr"/>
 *           &lt;element name="w" type="{urn:zimbraMail}wikiIdsAttr"/>
 *           &lt;element name="doc" type="{urn:zimbraMail}docIdsAttr"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "syncFolder", propOrder = {
    "tagOrCOrChat"
})
public class testSyncFolder
    extends testFolder
{

    @XmlElements({
        @XmlElement(name = "cn", type = testContactIdsAttr.class),
        @XmlElement(name = "appt", type = testApptIdsAttr.class),
        @XmlElement(name = "c", type = testConvIdsAttr.class),
        @XmlElement(name = "task", type = testTaskIdsAttr.class),
        @XmlElement(name = "doc", type = testDocIdsAttr.class),
        @XmlElement(name = "w", type = testWikiIdsAttr.class),
        @XmlElement(name = "tag", type = testTagIdsAttr.class),
        @XmlElement(name = "chat", type = testChatIdsAttr.class),
        @XmlElement(name = "m", type = testMsgIdsAttr.class),
        @XmlElement(name = "notes", type = testNoteIdsAttr.class)
    })
    protected List<testIdsAttr> tagOrCOrChat;

    /**
     * Gets the value of the tagOrCOrChat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tagOrCOrChat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTagOrCOrChat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link testContactIdsAttr }
     * {@link testApptIdsAttr }
     * {@link testConvIdsAttr }
     * {@link testTaskIdsAttr }
     * {@link testDocIdsAttr }
     * {@link testWikiIdsAttr }
     * {@link testTagIdsAttr }
     * {@link testChatIdsAttr }
     * {@link testMsgIdsAttr }
     * {@link testNoteIdsAttr }
     * 
     * 
     */
    public List<testIdsAttr> getTagOrCOrChat() {
        if (tagOrCOrChat == null) {
            tagOrCOrChat = new ArrayList<testIdsAttr>();
        }
        return this.tagOrCOrChat;
    }

}
