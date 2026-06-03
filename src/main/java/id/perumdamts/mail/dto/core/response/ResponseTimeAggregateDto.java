package id.perumdamts.mail.dto.core.response;

public record ResponseTimeAggregateDto(
        Long count,
        Double avgSeconds,
        Double p50Seconds,
        Double p90Seconds,
        Double p99Seconds,
        Double minSeconds,
        Double maxSeconds,
        String avgFormatted,
        String p50Formatted,
        String p90Formatted
) {
    public static ResponseTimeAggregateDto empty() {
        return new ResponseTimeAggregateDto(
                0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                "0s", "0s", "0s"
        );
    }

    public static ResponseTimeAggregateDto fromStats(ResponseTimeStatsDto stats) {
        if (stats == null || stats.count() == 0) {
            return empty();
        }
        return new ResponseTimeAggregateDto(
                stats.count(),
                stats.avg(),
                stats.p50(),
                stats.p90(),
                stats.p99(),
                stats.min(),
                stats.max(),
                formatDuration(stats.avg()),
                formatDuration(stats.p50()),
                formatDuration(stats.p90())
        );
    }

    private static String formatDuration(Double seconds) {
        if (seconds == null) return "0s";
        if (seconds < 60) return String.format("%.0fs", seconds);
        if (seconds < 3600) return String.format("%.1fm", seconds / 60);
        if (seconds < 86400) return String.format("%.1fh", seconds / 3600);
        return String.format("%.1fd", seconds / 86400);
    }
}