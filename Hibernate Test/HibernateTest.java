import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateTest {

	public static void main( String[] args ) {
		Table_appointment app = new Table_appointment();
		app.setAll_day(false);
		app.setApplication_id("1");
		app.setAppointment_type("appointment-self-service");
		app.setCalendar_id(20);
		app.setClient_id("notifive_client_1");
		app.setTitle("Hibernate Test");
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(app);
		session.getTransaction().commit();
	}

}
