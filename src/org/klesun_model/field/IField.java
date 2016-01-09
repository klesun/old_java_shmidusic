package org.klesun_model.field;

// the interface represents a field of storable model
// field provides abilities to parse/encode value to/from json

/** @TODO: probably, IModel should be IField too */

public interface IField<E>
{
	/** @return something that can be used as value in json structure
	 * type one of: [String, Integer, Float, JSONObject, JSONArray] */
	Object getJsonValue();
	/** @param jsonValue - type one of [String, Integer, Float, JSONObject, JSONArray] */
	void setJsonValue(Object jsonValue);

	/** @return - whether the field must me stored. using this field cuz
	 * have some optional fields, that likely always will keep default value,
	 * no need to trash readable file */
	Boolean mustBeStored();

	/** @return ... erm, i suppose, you can setJsonValue to Array... or no! */
	/** @TODO: reconstructFromJson should be called ONLY upon instantiation */
	Boolean isFinal();
}
