package io.github.townyadvanced.townyprovinces.jobs.land_validation;

public enum LandValidationJobStatus {
	START_REQUESTED("land_validation_job_status_start_requested"),
	STARTED("land_validation_job_status_started"),
	STOP_REQUESTED("land_validation_job_status_stop_requested"),
	STOPPED("land_validation_job_status_stopped"),
	RESTART_REQUESTED("land_validation_job_status_restart_requested"),
	PAUSE_REQUESTED("land_validation_job_status_pause_requested"),
	PAUSED("land_validation_job_status_paused");
	
	final String languageKey;
	
	LandValidationJobStatus(String languageKey) {
		this.languageKey = languageKey;
	}
	
	public String getLanguageKey() {
		return languageKey;
	}
}
