package xuml.tools.datastore.gae;

import xuml.tools.datastore.DatastoreText;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

/**
 * Implements storage of string properties in Google App Engine's Big Table
 * datastore.
 * 
 * @author dave
 * 
 */
public class DatastoreTextGae implements DatastoreText {

    private static DatastoreText datastore = new DatastoreTextGae();

    public static DatastoreText instance() {
        return datastore;
    }

    @Override
    public void put(String kind, String name, String property, String value) {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Key k = KeyFactory.createKey(kind, name);
        System.out.println("putting " + k + "=" + value);
        Entity ent = new Entity(k);
        ent.setProperty(property, new Text(value));
        ds.put(ent);
    }

    @Override
    public String get(String kind, String name, String property) {
        System.out.println("getting");
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Key k = KeyFactory.createKey(kind, name);
        String result;
        try {
            Entity ent = ds.get(k);
            System.out.println(ent.getProperties());
            Text prop = (Text) ent.getProperty(property);
            System.out.println("Datastore.get " + k + "= " + prop);
            if (prop != null)
                result = prop.getValue();
            else
                result = null;
        } catch (EntityNotFoundException e) {
            System.out.println(e.getMessage());
            result = null;
        }
        System.out.println("get returns " + result);
        return result;
    }

}
