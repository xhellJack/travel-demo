-- src/main/resources/data.sql
-- 针对 MySQL 数据库的初始化数据脚本

-- 清理旧数据 (可选，如果希望每次启动都是全新的数据)
-- 请注意：在生产环境中不要使用这些DELETE语句！
-- SET FOREIGN_KEY_CHECKS = 0; -- 临时禁用外键检查
-- DELETE FROM itinerary_attractions;
-- DELETE FROM itinerary_tags_join;
-- DELETE FROM itineraries;
-- DELETE FROM user_favorites;
-- DELETE FROM reviews;
-- DELETE FROM attraction_tags_join;
-- DELETE FROM attractions;
-- DELETE FROM user_preferred_tags;
-- DELETE FROM user_roles;
-- DELETE FROM users;
-- DELETE FROM tags;
-- SET FOREIGN_KEY_CHECKS = 1; -- 重新启用外键检查

-- 重置自增ID (可选，仅用于测试，确保ID从1开始)
-- ALTER TABLE tags AUTO_INCREMENT = 1;
-- ALTER TABLE users AUTO_INCREMENT = 1;
-- ALTER TABLE attractions AUTO_INCREMENT = 1;
-- ALTER TABLE reviews AUTO_INCREMENT = 1;
-- ALTER TABLE itineraries AUTO_INCREMENT = 1;


-- 1. 插入标签 (tags)
-- (ID将从1开始自增)
INSERT INTO tags (name, tag_category, description) VALUES
                                                       ('历史古迹', '兴趣点', '探寻古老文明的印记，感受历史的厚重。'),
                                                       ('自然风光', '兴趣点', '沉浸于壮丽的山川湖海，呼吸自然的清新。'),
                                                       ('主题乐园', '活动类型', '充满欢乐与刺激的奇幻世界，适合所有年龄。'),
                                                       ('博物馆', '文化艺术', '浓缩人类智慧与创造的殿堂，开启知识之旅。'),
                                                       ('美食巡礼', '餐饮风味', '品尝地道风味，满足挑剔的味蕾。'),
                                                       ('购物天堂', '活动类型', '时尚与传统的交汇，尽享购物乐趣。'),
                                                       ('户外徒步', '户外活动', '挑战自我，用脚步丈量世界的美丽。'),
                                                       ('阳光海滩', '自然风光', '享受阳光、沙滩与海浪的悠闲时光。'),
                                                       ('亲子时光', '适合人群', '专为家庭设计，共享温馨欢乐的亲子时刻。'),
                                                       ('浪漫情侣', '适合人群', '为爱侣打造的浪漫私密空间。'),
                                                       ('经济实惠', '消费水平', '精打细算也能享受的精彩旅程。'),
                                                       ('奢华体验', '消费水平', '尊享顶级服务与设施的非凡之旅。'),
                                                       ('现代都市', '兴趣点', '感受国际化大都市的繁华与活力。'),
                                                       ('温泉度假', '休闲放松', '在氤氲泉水中洗去疲惫，焕发身心活力。'),
                                                       ('文化遗产', '文化艺术', '被联合国教科文组织认定的具有突出普遍价值的地点。');

-- 2. 插入用户 (users)
-- !!! 重要: 请将 @encodedPassword 替换为您用 BCryptPasswordEncoder.encode("一个明文密码") 生成的实际哈希值 !!!
-- 例如，假设 "password123" 的哈希值是 "$2a$10$EixZaY597.M2UhQL4SAC3.sZ9DPxYnOAFn1z8wfcqgXwqcU6uQx3q"
SET @encodedPassword = '$2a$10$EixZaY597.M2UhQL4SAC3.sZ9DPxYnOAFn1z8wfcqgXwqcU6uQx3q'; -- 这是一个示例值，请替换

INSERT INTO users (username, password, email, first_name, last_name, date_of_birth, gender, city, country, is_active, email_verified, created_at, updated_at) VALUES
                                                                                                                                                                  ('admin', @encodedPassword, 'admin@travel.com', '管理员', '系统', '1985-01-01', 'MALE', '北京', '中国', 1, 1, NOW(), NOW()),
                                                                                                                                                                  ('alice_adventure', @encodedPassword, 'alice@example.com', '爱丽丝', '王', '1992-07-22', 'FEMALE', '上海', '中国', 1, 1, NOW(), NOW()),
                                                                                                                                                                  ('bob_budget', @encodedPassword, 'bob@example.com', '鲍勃', '李', '1988-11-05', 'MALE', '广州', '中国', 1, 0, NOW(), NOW()),
                                                                                                                                                                  ('charlie_culture', @encodedPassword, 'charlie@example.com', '查理', '张', '1998-03-12', 'MALE', '成都', '中国', 1, 1, NOW(), NOW()),
                                                                                                                                                                  ('diana_deluxe', @encodedPassword, 'diana@example.com', '戴安娜', '刘', '1990-09-30', 'FEMALE', '深圳', '中国', 0, 1, NOW(), NOW()); -- Diana is inactive

-- 3. 为用户分配角色 (user_roles)
-- 假设用户ID: admin=1, alice_adventure=2, bob_budget=3, charlie_culture=4, diana_deluxe=5
INSERT INTO user_roles (user_id, role) VALUES
                                                (1, 'ADMIN'),
                                                (1, 'USER'),
                                                (2, 'USER'),
                                                (3, 'USER'),
                                                (4, 'USER'),
                                                (5, 'USER');

-- 4. 为用户设置偏好标签 (user_preferred_tags)
-- 假设标签ID: 自然风光=2, 主题乐园=3, 博物馆=4, 美食巡礼=5, 户外徒步=7, 奢华体验=12, 文化遗产=15
INSERT INTO user_preferred_tags (user_id, tag_id) VALUES
                                                      (2, 2), (2, 7), (2, 3), -- Alice 喜欢自然、徒步、主题乐园
                                                      (3, 5), (3, 11),        -- Bob 喜欢美食、经济实惠
                                                      (4, 1), (4, 4), (4, 15), -- Charlie 喜欢历史古迹、博物馆、文化遗产
                                                      (5, 12), (5, 6);        -- Diana 喜欢奢华体验、购物

-- 5. 插入景点 (attractions)
-- (ID将从1开始自增)
INSERT INTO attractions (name, description, location, address, opening_hours, ticket_price, image_url, latitude, longitude, category, average_rating, rating_count, contact_phone, website, estimated_duration_hours, best_time_to_visit, status, created_at, updated_at) VALUES
                                                                                                                                                                                                                                                                              ('长城 - 慕田峪段', '中国古代伟大的防御工程，慕田峪段风景秀丽，较为完整。', '北京怀柔区', '北京市怀柔区渤海镇慕田峪村', '07:30-17:30', 40.00, 'https://images.unsplash.com/photo-1529973561033-9d231a1aab73?q=80', 40.4401, 116.5652, '历史古迹', 4.7, 2500, '010-61626022', 'http://www.mutianyugreatwall.com/', 4.0, '春/秋季', 'OPEN', NOW(), NOW()),
                                                                                                                                                                                                                                                                              ('西湖', '中国十大风景名胜之一，以湖光山色和众多名胜古迹闻名。', '浙江杭州', '浙江省杭州市西湖区龙井路1号', '全天开放 (部分景点收费)', 0.00, 'https://images.unsplash.com/photo-1590364377894-0cc3369f5497?q=80', 30.2460, 120.1551, '自然风光', 4.9, 5500, NULL, 'http://www.hzwestlake.gov.cn/', 6.0, '四季皆宜', 'OPEN', NOW(), NOW()),
                                                                                                                                                                                                                                                                              ('兵马俑博物馆', '秦始皇陵的陪葬坑，被誉为“世界第八大奇迹”。', '陕西西安临潼区', '陕西省西安市临潼区秦陵北路', '08:30-17:00', 120.00, 'https://images.unsplash.com/photo-1580252133465-a29b7849869a?q=80', 34.3853, 109.2538, '文化遗产', 4.8, 4800, '029-81399001', 'http://www.bmy.com.cn/', 3.0, '全年', 'OPEN', NOW(), NOW()),
                                                                                                                                                                                                                                                                              ('外滩', '上海的标志性地标，欣赏黄浦江两岸的万国建筑博览群和现代摩天大楼。', '上海黄浦区', '上海市黄浦区中山东一路', '全天开放', 0.00, 'https://images.unsplash.com/photo-1506240597479-76390a7b3a55?q=80', 31.2349, 121.4913, '现代都市', 4.6, 6200, NULL, NULL, 2.0, '夜晚', 'OPEN', NOW(), NOW()),
                                                                                                                                                                                                                                                                              ('张家界国家森林公园', '中国第一个国家森林公园，以奇特的石英砂岩峰林地貌闻名。', '湖南张家界', '湖南省张家界市武陵源区金鞭路279号', '07:00-18:00', 224.00, 'https://images.unsplash.com/photo-1600329567009-27d599a78a75?q=80', 29.3511, 110.4591, '自然风光', 4.8, 3200, '0744-5718833', 'http://www.zjjpark.com/', 16.0, '4月-10月', 'OPEN', NOW(), NOW()),
                                                                                                                                                                                                                                                                              ('欢乐谷（北京）', '大型主题公园，包含多种刺激游乐设施和精彩表演。', '北京朝阳区', '北京市朝阳区东四环小武基北路', '09:30-21:00 (根据季节调整)', 299.00, 'https://example.com/images/happyvalley.jpg', 39.8665, 116.4905, '主题乐园', 4.5, 1500, '010-67389898', 'http://bj.happyvalley.cn/', 8.0, '周末/节假日', 'OPEN', NOW(), NOW());

-- 6. 为景点关联标签 (attraction_tags_join)
-- 景点ID: 长城=1, 西湖=2, 兵马俑=3, 外滩=4, 张家界=5, 欢乐谷=6
-- 标签ID: 历史古迹=1, 自然风光=2, 主题乐园=3, 户外徒步=7, 文化遗产=15, 现代都市=13, 亲子时光=9
INSERT INTO attraction_tags_join (attraction_id, tag_id) VALUES
                                                             (1, 1), (1, 2), (1, 7), (1, 15), -- 长城
                                                             (2, 2), (2, 10),                -- 西湖
                                                             (3, 1), (3, 4), (3, 15),        -- 兵马俑
                                                             (4, 13), (4, 6),                -- 外滩
                                                             (5, 2), (5, 7),                 -- 张家界
                                                             (6, 3), (6, 9);                 -- 欢乐谷

-- 7. 插入评价 (reviews)
-- 用户ID: alice=2, bob=3, charlie=4
-- 景点ID: 长城=1, 西湖=2, 兵马俑=3
INSERT INTO reviews (user_id, attraction_id, rating, title, comment, visit_date, image_url, helpful_count, created_at, updated_at) VALUES
                                                                                                                                       (2, 1, 5, '慕田峪长城太美了！', '风景非常棒，人相对较少，缆车很方便。推荐！', '2024-10-05', 'https://example.com/reviews/alice_greatwall.jpg', 22, NOW(), NOW()),
                                                                                                                                       (3, 2, 4, '西湖很惬意', '租了自行车环湖，感觉很好，就是有些地方人多。', '2024-09-20', NULL, 12, NOW(), NOW()),
                                                                                                                                       (4, 3, 5, '震撼的兵马俑', '亲眼看到兵马俑非常震撼，历史的厚重感扑面而来。导游讲解很重要。', '2024-11-01', 'https://example.com/reviews/charlie_terracotta.jpg', 35, NOW(), NOW()),
                                                                                                                                       (2, 3, 4, '值得一看', '人很多，但确实是奇迹。一号坑最壮观。', '2024-11-15', NULL, 8, NOW(), NOW());

-- 更新景点的评分和评价数量 (这一步通常由应用逻辑完成，这里手动模拟初始值，或者在插入review后手动更新)
UPDATE attractions SET average_rating = 4.75, rating_count = 2 WHERE id = 1; -- 长城 (5+4.5)/2 -> 假设另一条评价是4.5（未在此脚本中）
UPDATE attractions SET average_rating = 4.0, rating_count = 1 WHERE id = 2; -- 西湖
UPDATE attractions SET average_rating = 4.5, rating_count = 2 WHERE id = 3; -- 兵马俑 (5+4)/2

-- 8. 为用户收藏景点 (user_favorites)
-- 用户ID: alice=2, bob=3, charlie=4
-- 景点ID: 长城=1, 西湖=2, 兵马俑=3, 外滩=4
INSERT INTO user_favorites (user_id, attraction_id, created_at) VALUES
                                                                    (2, 1, NOW()), -- Alice 收藏 长城
                                                                    (2, 4, NOW()), -- Alice 收藏 外滩
                                                                    (3, 2, NOW()), -- Bob 收藏 西湖
                                                                    (3, 3, NOW()), -- Bob 收藏 兵马俑
                                                                    (4, 1, NOW()), -- Charlie 收藏 长城
                                                                    (4, 3, NOW()); -- Charlie 收藏 兵马俑

-- 9. 插入行程 (itineraries)
-- 用户ID: alice=2, bob=3
INSERT INTO itineraries (user_id, name, description, start_date, end_date, is_public, status, total_estimated_cost, cover_image_url, created_at, updated_at) VALUES
                                                                                                                                                                 (2, '爱丽丝的周末北京精华游', '利用周末时间，游览北京最具代表性的几个景点。', '2025-05-23', '2025-05-25', 1, 'PLANNING', 1200.00, 'https://example.com/itineraries/beijing_weekend.jpg', NOW(), NOW()),
                                                                                                                                                                 (3, '鲍勃的经济舒适杭州两日行', '预算有限，但也要玩得开心！探索杭州的自然与人文。', '2025-06-10', '2025-06-11', 0, 'CONFIRMED', 650.00, 'https://example.com/itineraries/hangzhou_budget.jpg', NOW(), NOW());
-- 行程ID: 北京精华游=1, 杭州两日行=2 (假设)

-- 10. 为行程关联标签 (itinerary_tags_join)
-- 行程ID: 北京精华游=1, 杭州两日行=2
-- 标签ID: 历史古迹=1, 自然风光=2, 经济实惠=11, 现代都市=13
INSERT INTO itinerary_tags_join (itinerary_id, tag_id) VALUES
                                                           (1, 1), (1, 13), -- 北京精华游 - 历史古迹, 现代都市
                                                           (2, 2), (2, 11); -- 杭州两日行 - 自然风光, 经济实惠

-- 11. 为行程添加景点安排 (itinerary_attractions)
-- 行程ID: 北京精华游=1, 杭州两日行=2
-- 景点ID: 长城=1, 西湖=2, 外滩=4
INSERT INTO itinerary_attractions (itinerary_id, attraction_id, visit_date, order_in_itinerary, start_time, end_time, notes, transportation_to_next_notes) VALUES
                                                                                                                                                               (1, 1, '2025-05-24', 1, '09:00:00', '16:00:00', '慕田峪长城，准备好徒步！', '包车前往下一个景点'),
                                                                                                                                                               (1, 4, '2025-05-25', 2, '19:00:00', '21:00:00', '晚上看外滩夜景。', '地铁'),
                                                                                                                                                               (2, 2, '2025-06-10', 1, '10:00:00', '17:00:00', '环西湖骑行，苏堤春晓。', '步行或公交');