package goplaces.models;

/**
 * Objects of this class are used for accepting JSON corresponding to a string of place_ids.
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE) // This is to include only the fields we want in the generated JSON
public class PlaceIDS {
    String place_ids;

    public PlaceIDS(String place_ids) {
        this.place_ids = place_ids;
    }

    public PlaceIDS(){

    }

    @XmlElement
    public String getPlace_ids() {
        return place_ids;
    }

    @XmlElement
    public void setPlace_ids(String place_ids) {
        this.place_ids = place_ids;
    }
}
