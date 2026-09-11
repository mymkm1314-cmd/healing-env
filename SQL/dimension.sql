-- 维度满分以该维度下“基本项满分 + 加分项满分”合计为准。
-- 清理早期模拟数据中重复创建的无引用维度。
DELETE FROM yllypj.dimension WHERE id IN (6, 7, 8, 9, 10);

INSERT INTO yllypj.dimension
    (id, name, sort, max_score, description, create_time, update_time, deleted)
VALUES
    (1, '人性化', 1, 25.00, '以患者为中心的服务与流程', '2026-09-08 15:45:16', null, 0),
    (2, '功能空间', 2, 25.00, '空间布局与功能配置', '2026-09-08 15:45:16', null, 0),
    (3, '物理环境', 3, 23.00, '光、声、热、气等物理条件', '2026-09-08 15:45:16', null, 0),
    (4, '社会支持', 4, 21.00, '心理与社会服务支持', '2026-09-08 15:45:16', null, 0),
    (5, '艺术疗愈', 5, 26.00, '艺术与自然元素的疗愈介入', '2026-09-08 15:45:16', null, 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort = VALUES(sort),
    max_score = VALUES(max_score),
    description = VALUES(description),
    update_time = VALUES(update_time),
    deleted = VALUES(deleted);
