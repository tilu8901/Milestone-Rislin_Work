import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Table_appointment {
	@Id
	private int appointment_id;
	private int transaction_id;
	private String client_id;
	private String application_id;
	private String title;
	private String name;
	private Date start_date;
	private Date end_date;
	private int duration;
	private boolean all_day;
	private String mobile;
	private String confirmation_status;
	private String location;
	private String notes;
	private String sync_status;
	private int calendar_id;
	private String google_event_id;
	private int no_of_participants;
	private String participant_type;
	private String appointment_type;
	public int getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id( int transaction_id ) {
		this.transaction_id = transaction_id;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id( String client_id ) {
		this.client_id = client_id;
	}
	public String getApplication_id() {
		return application_id;
	}
	public void setApplication_id( String application_id ) {
		this.application_id = application_id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle( String title ) {
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName( String name ) {
		this.name = name;
	}
	public Date getStart_date() {
		return start_date;
	}
	public void setStart_date( Date start_date ) {
		this.start_date = start_date;
	}
	public Date getEnd_date() {
		return end_date;
	}
	public void setEnd_date( Date end_date ) {
		this.end_date = end_date;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration( int duration ) {
		this.duration = duration;
	}
	public boolean isAll_day() {
		return all_day;
	}
	public void setAll_day( boolean all_day ) {
		this.all_day = all_day;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile( String mobile ) {
		this.mobile = mobile;
	}
	public String getConfirmation_status() {
		return confirmation_status;
	}
	public void setConfirmation_status( String confirmation_status ) {
		this.confirmation_status = confirmation_status;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation( String location ) {
		this.location = location;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes( String notes ) {
		this.notes = notes;
	}
	public String getSync_status() {
		return sync_status;
	}
	public void setSync_status( String sync_status ) {
		this.sync_status = sync_status;
	}
	public int getCalendar_id() {
		return calendar_id;
	}
	public void setCalendar_id( int calendar_id ) {
		this.calendar_id = calendar_id;
	}
	public int getNo_of_participants() {
		return no_of_participants;
	}
	public void setNo_of_participants( int no_of_participants ) {
		this.no_of_participants = no_of_participants;
	}
	public String getGoogle_event_id() {
		return google_event_id;
	}
	public void setGoogle_event_id( String google_event_id ) {
		this.google_event_id = google_event_id;
	}
	public String getParticipant_type() {
		return participant_type;
	}
	public void setParticipant_type( String participant_type ) {
		this.participant_type = participant_type;
	}
	public String getAppointment_type() {
		return appointment_type;
	}
	public void setAppointment_type( String appointment_type ) {
		this.appointment_type = appointment_type;
	}
}
