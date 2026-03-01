package database

import (
	"encoding/json"
	"errors"
)

type StringArray []string

func (a *StringArray) Scan(value interface{}) error {
	b, ok := value.([]byte)
	if !ok {
		s, okStr := value.(string)
		if !okStr {
			return errors.New("type assertion to []byte or string failed")
		}
		b = []byte(s)
	}
	return json.Unmarshal(b, &a)
}

func (a StringArray) Value() (string, error) {
	marshal, err := json.Marshal(a)
	if err != nil {
		return "", err
	}
	return string(marshal), nil
}
