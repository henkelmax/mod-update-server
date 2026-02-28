package log

import (
	"context"
	"fmt"
	"io"
	"log/slog"
	"os"
	"time"
)

type LogHandler struct {
	out   io.Writer
	level slog.Leveler
}

func (h *LogHandler) Enabled(ctx context.Context, level slog.Level) bool {
	return level >= h.level.Level()
}

func (h *LogHandler) Handle(ctx context.Context, r slog.Record) error {
	timeStr := r.Time.Format(time.RFC3339)
	levelStr := r.Level.String()
	_, err := fmt.Fprintf(h.out, "[%s][%s] %s\n", timeStr, levelStr, r.Message)
	return err
}

func (h *LogHandler) WithAttrs(attrs []slog.Attr) slog.Handler {
	return h
}

func (h *LogHandler) WithGroup(name string) slog.Handler {
	return h
}

func InitializeLogging() {
	handler := LogHandler{os.Stdout, slog.LevelInfo}
	logger := slog.New(&handler)
	slog.SetDefault(logger)
}
