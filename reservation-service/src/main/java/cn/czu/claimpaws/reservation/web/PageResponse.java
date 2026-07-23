package cn.czu.claimpaws.reservation.web;

import java.util.List;

public record PageResponse<T>(List<T> records, int page, int size, long total) {}
