
package generated.zcsclient.admin;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dedupStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dedupStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="running"/>
 *     &lt;enumeration value="stopped"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "dedupStatus")
@XmlEnum
public enum testDedupStatus {

    @XmlEnumValue("running")
    RUNNING("running"),
    @XmlEnumValue("stopped")
    STOPPED("stopped");
    private final String value;

    testDedupStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static testDedupStatus fromValue(String v) {
        for (testDedupStatus c: testDedupStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
