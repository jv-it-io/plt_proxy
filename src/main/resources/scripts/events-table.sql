CREATE TABLE IF NOT EXISTS events  (
	id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,
	event_id LONG NOT NULL,
	event_timestamp TEXT,
	event_type TEXT,
	json TEXT
) ;