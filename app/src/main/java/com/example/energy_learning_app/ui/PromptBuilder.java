package com.example.energy_learning_app.ui;

public class PromptBuilder {

    private PromptBuilder() {
        // 不讓人 new（工具類）
    }

    public static String buildSlideTutorPrompt(float massKg, float heightM, float mu, float angleDeg,
                                               float pe0J, float keEndJ, float heatEndJ )
    {
        return ""
                + "你是一位國中到高中程度的物理老師。\n"
                + "請用「簡單、口語、一步一步」的方式解釋能量轉換，並根據同一份數據出 4 題單選題。\n\n"

                + "【情境】\n"
                + "小球沿斜坡滑下，有摩擦。\n\n"

                + "【使用者設定】\n"
                + "高度 h = " + heightM + " m\n"
                + "質量 m = " + massKg + " kg\n"
                + "摩擦係數 μ = " + mu + "\n"
                + "斜坡角度 θ = " + angleDeg + "°\n\n"

                + "【模擬結果（已計算）】\n"
                + "初始位能 PE0 = " + pe0J + " J\n"
                + "底部動能 KE_end = " + keEndJ + " J\n"
                + "摩擦轉換成的熱能 Heat_end = " + heatEndJ + " J\n\n"

                + "【任務 1：解說 explanation】\n"
                + "- 解釋位能→動能、摩擦→熱能\n"
                + "- 解釋為何仍符合能量守恆（PE0 ≈ KE_end + Heat_end）\n"
                + "- 避免太多公式，像在跟學生講\n\n"

                + "【任務 2：出 4 題單選題 quiz】\n"
                + "- 每題 A/B/C/D 四個選項\n"
                + "- 題目圍繞：能量守恆、摩擦造成能量轉換、改變 μ/h/m 的影響\n"
                + "- 每題附正確答案 answer（只填 A/B/C/D）\n"
                + "- 每題附簡短解釋 rationale\n\n"

                + "【輸出格式（非常重要）】\n"
                + "請只輸出 JSON，完全不要多任何文字。\n"
                + "{\n"
                + "  \"explanation\": \"string\",\n"
                + "  \"quiz\": [\n"
                + "    {\n"
                + "      \"question\": \"string\",\n"
                + "      \"options\": {\"A\":\"string\",\"B\":\"string\",\"C\":\"string\",\"D\":\"string\"},\n"
                + "      \"answer\": \"A|B|C|D\",\n"
                + "      \"rationale\": \"string\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n";
    }

    public static String buildFrictionTutorPrompt(float massKg, float mu, float normalN,
                                                  float distanceM, float frictionWorkJ, float heatJ)
    {
        return ""
                + "你是一位國中到高中程度的物理老師。\n"
                + "請用「簡單、口語、一步一步」的方式解釋『摩擦把機械能轉成熱能』，並根據同一份數據出 4 題單選題。\n\n"

                + "【情境】\n"
                + "物體在粗糙表面上滑動，存在摩擦力。\n\n"

                + "【使用者設定】\n"
                + "質量 m = " + massKg + " kg\n"
                + "摩擦係數 μ = " + mu + "\n"
                + "正向力 N ≈ " + normalN + " N\n"
                + "滑動距離 s = " + distanceM + " m\n\n"

                + "【模擬結果（已計算）】\n"
                + "摩擦做功 W_f = " + frictionWorkJ + " J（通常為負，代表能量被消耗）\n"
                + "轉換成熱能 Heat = " + heatJ + " J\n\n"

                + "【任務 1：解說 explanation】\n"
                + "- 解釋摩擦力如何消耗機械能、變成熱能\n"
                + "- 強調：能量沒有消失，只是形式改變\n"
                + "- 避免太多公式，像在跟學生講\n\n"

                + "【任務 2：出 4 題單選題 quiz】\n"
                + "- 每題 A/B/C/D 四個選項\n"
                + "- 題目圍繞：摩擦力做功、熱能增加、改變 μ 或距離 s 的影響\n"
                + "- 每題附正確答案 answer（只填 A/B/C/D）\n"
                + "- 每題附簡短解釋 rationale\n\n"

                + "【輸出格式（非常重要）】\n"
                + "請只輸出 JSON，完全不要多任何文字。\n"
                + "{\n"
                + "  \"explanation\": \"string\",\n"
                + "  \"quiz\": [\n"
                + "    {\n"
                + "      \"question\": \"string\",\n"
                + "      \"options\": {\"A\":\"string\",\"B\":\"string\",\"C\":\"string\",\"D\":\"string\"},\n"
                + "      \"answer\": \"A|B|C|D\",\n"
                + "      \"rationale\": \"string\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n";
    }

    public static String buildHeatTutorPrompt(String materialName, float massKg, float specificHeat,
                                              float tempStartC, float tempEndC, float heatJ)
    {
        return ""
                + "你是一位國中到高中程度的物理老師。\n"
                + "請用「簡單、口語、一步一步」的方式解釋『加熱讓內能上升、溫度上升』，並根據同一份數據出 4 題單選題。\n\n"

                + "【情境】\n"
                + "對物體加熱，溫度上升。\n\n"

                + "【使用者設定】\n"
                + "材質 = " + (materialName == null ? "未知" : materialName) + "\n"
                + "質量 m = " + massKg + " kg\n"
                + "比熱 c = " + specificHeat + " (J/kg·°C)\n"
                + "起始溫度 T1 = " + tempStartC + " °C\n"
                + "結束溫度 T2 = " + tempEndC + " °C\n\n"

                + "【模擬結果（已計算）】\n"
                + "吸收熱量 Q = " + heatJ + " J\n\n"

                + "【任務 1：解說 explanation】\n"
                + "- 解釋：加熱讓物體內能增加，所以溫度上升\n"
                + "- 連結到 Q, m, c, ΔT 的意義（可以口語，不要推太多公式）\n"
                + "- 強調：吸收的熱量去哪裡了？→ 變成內能\n\n"

                + "【任務 2：出 4 題單選題 quiz】\n"
                + "- 每題 A/B/C/D 四個選項\n"
                + "- 題目圍繞：改變質量 m、比熱 c、溫差 ΔT 對熱量 Q 的影響\n"
                + "- 每題附正確答案 answer（只填 A/B/C/D）\n"
                + "- 每題附簡短解釋 rationale\n\n"

                + "【輸出格式（非常重要）】\n"
                + "請只輸出 JSON，完全不要多任何文字。\n"
                + "{\n"
                + "  \"explanation\": \"string\",\n"
                + "  \"quiz\": [\n"
                + "    {\n"
                + "      \"question\": \"string\",\n"
                + "      \"options\": {\"A\":\"string\",\"B\":\"string\",\"C\":\"string\",\"D\":\"string\"},\n"
                + "      \"answer\": \"A|B|C|D\",\n"
                + "      \"rationale\": \"string\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n";
    }
}
