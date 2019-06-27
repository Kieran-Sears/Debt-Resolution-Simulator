package util
import java.util.UUID

import simulator.model._

class MockData {

  val arrears1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 50, 500)
  val satisfaction1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 50)
  val age1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 18, 25)
  val income1 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 15000, 22000)

  val arrears2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 500, 2000)
  val satisfaction2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 30, 80)
  val age2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 25, 40)
  val income2 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 21000, 40000)

  val arrears3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 5000)
  val satisfaction3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 0, 100)
  val age3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 35, 55)
  val income3 = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 30000, 50000)

  val rent = OptionConfig(UUID.randomUUID(), "Rent", 50)
  val homeowner = OptionConfig(UUID.randomUUID(), "Homeowner", 20)
  val councilHousing = OptionConfig(UUID.randomUUID(), "Council housing", 30)
  val tenure = CategoricalConfig(UUID.randomUUID(), List(rent.id, homeowner.id, councilHousing.id))

  val arrearsG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 10, 50000)
  val satisfactionG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 0, 100)
  val ageG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 18, 85)
  val incomeG = ScalarConfig(id = UUID.randomUUID(), VarianceEnum.None, 15000, 22000)

  val rentG = OptionConfig(UUID.randomUUID(), "Rent", 50)
  val homeownerG = OptionConfig(UUID.randomUUID(), "Homeowner", 20)
  val councilHousingG = OptionConfig(UUID.randomUUID(), "Council housing", 30)
  val emergencyG = OptionConfig(UUID.randomUUID(), "Emergency", 30)
  val tenureG =
    CategoricalConfig(id = UUID.randomUUID(), List(rentG.id, homeownerG.id, councilHousingG.id, emergencyG.id))

  val arrearsAttG =
    AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrearsG.id, AttributeEnum.Global)
  val satisfactionAttG =
    AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfactionG.id, AttributeEnum.Global)
  val ageAttG = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = ageG.id, AttributeEnum.Global)
  val incomeAttG = AttributeConfig(id = UUID.randomUUID(), name = "Income", value = incomeG.id, AttributeEnum.Global)
  val tenureAttG = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenureG.id, AttributeEnum.Global)

  val arrearsAtt1 =
    AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears1.id, AttributeEnum.Override)
  val satisfactionAtt1 =
    AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction1.id, AttributeEnum.Override)
  val ageAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age1.id, AttributeEnum.Override)
  val incomeAtt1 =
    AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income1.id, AttributeEnum.Override)
  val tenureAtt1 = AttributeConfig(id = UUID.randomUUID(), name = "Tenure", value = tenure.id, AttributeEnum.Override)

  val arrearsAtt2 =
    AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears2.id, AttributeEnum.Override)
  val satisfactionAtt2 =
    AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction2.id, AttributeEnum.Override)
  val ageAtt2 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age2.id, AttributeEnum.Override)
  val incomeAtt2 =
    AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income2.id, AttributeEnum.Override)

  val arrearsAtt3 =
    AttributeConfig(id = UUID.randomUUID(), name = "Arrears", value = arrears3.id, AttributeEnum.Override)
  val satisfactionAtt3 =
    AttributeConfig(id = UUID.randomUUID(), name = "Satisfaction", value = satisfaction3.id, AttributeEnum.Override)
  val ageAtt3 = AttributeConfig(id = UUID.randomUUID(), name = "Age", value = age3.id, AttributeEnum.Override)
  val incomeAtt3 =
    AttributeConfig(id = UUID.randomUUID(), name = "Income", value = income3.id, AttributeEnum.Override)

  val customerConfig1 = CustomerConfig(
    id = UUID.randomUUID(),
    name = "LowRoller",
    attributeOverrides = List(arrearsAtt1.id, ageAtt1.id, satisfactionAtt1.id, incomeAtt1.id, tenureAtt1.id),
    proportion = 20
  )

  val customerConfig2 = CustomerConfig(
    id = UUID.randomUUID(),
    name = "MidRoller",
    attributeOverrides = List(arrearsAtt2.id, ageAtt2.id, satisfactionAtt2.id, incomeAtt2.id),
    proportion = 20
  )

  val customerConfig3 = CustomerConfig(
    id = UUID.randomUUID(),
    name = "HighRoller",
    attributeOverrides = List(arrearsAtt3.id, ageAtt3.id, satisfactionAtt3.id, incomeAtt3.id),
    proportion = 20
  )

  val scalarConfigs = List(
    arrearsG,
    satisfactionG,
    ageG,
    incomeG,
    arrears1,
    satisfaction1,
    age1,
    income1,
    arrears2,
    satisfaction2,
    age2,
    income2,
    arrears3,
    satisfaction3,
    age3,
    income3)

  val effect1 = EffectConfig(UUID.randomUUID(), "ZeroArrears", EffectEnum.Effect, "Arrears")
  val effect2 = EffectConfig(UUID.randomUUID(), "Satisfy", EffectEnum.Effect, "Satisfaction")
  val effect3 = EffectConfig(UUID.randomUUID(), "Cooperative", EffectEnum.Affect, "Arrears")
  val effect4 = EffectConfig(UUID.randomUUID(), "Dissatisfy", EffectEnum.Effect, "Satisfaction")

  val actionConf1 =
    ActionConfig(UUID.randomUUID(), "PayInFull", ActionEnum.Customer, List(effect1.id, effect2.id, effect3.id))
  val actionConf2 = ActionConfig(UUID.randomUUID(), "Litigate", ActionEnum.Agent, List(effect1.id, effect4.id))

  val effectConfigs = List(effect1, effect2, effect3, effect4)
  val actionConfigs = List(actionConf1, actionConf2)
  val categoricalConfigs = List(tenure, tenureG)
  val optionConfigs =
    List[OptionConfig](rent, homeowner, councilHousing, rentG, homeownerG, councilHousingG, emergencyG)
  val customerConfigs = List(customerConfig1, customerConfig2, customerConfig3)
  val simulationConfig = SimulationConfig(UUID.randomUUID(), 0, Some(100), 120)

  val attributeConfigs = List(
    ageAttG,
    incomeAttG,
    tenureAttG,
    arrearsAttG,
    satisfactionAttG,
    ageAtt1,
    incomeAtt1,
    tenureAtt1,
    arrearsAtt1,
    satisfactionAtt1,
    ageAtt2,
    incomeAtt2,
    arrearsAtt2,
    satisfactionAtt2,
    ageAtt3,
    incomeAtt3,
    arrearsAtt3,
    satisfactionAtt3
  )

  val configuration = Configurations(
    UUID.randomUUID(),
    customerConfigs,
    actionConfigs,
    effectConfigs,
    attributeConfigs,
    scalarConfigs,
    categoricalConfigs,
    optionConfigs,
    simulationConfig
  )

  val customer1 = Customer(
    UUID.fromString("2e2d89f8-0711-46e8-91f1-0bb6793d0d40"),
    "LowRoller",
    List(
      Attribute(UUID.fromString("6a0f3b05-c019-4769-8ecf-ba1f36c85ac0"), "Age", 21.5),
      Attribute(UUID.fromString("bd7b1430-2afb-4681-92d7-d2e310ed3b5b"), "Income", 18500.0),
      Attribute(UUID.fromString("a8a23588-d053-4993-9e1a-66b1669aa33d"), "Tenure", 0.0),
      Attribute(UUID.fromString("2c8af23b-a925-4a95-86f3-adc8adb958af"), "Arrears", 275.0),
      Attribute(UUID.fromString("3b507419-795e-4f51-95b6-cd3114c17a3c"), "Satisfaction", 30.0)
    ),
    None,
    None
  )

  val customer2 = Customer(
    UUID.fromString("d66010a4-6e9a-4d15-bbe8-1b847e133f80"),
    "MidRoller",
    List(
      Attribute(UUID.fromString("823e2475-0c41-45e4-9bbf-916a50352e28"), "Age", 32.5),
      Attribute(UUID.fromString("c6b67386-0b94-4cac-a279-76863c3be075"), "Income", 30500.0),
      Attribute(UUID.fromString("0c29c133-d704-4719-98ab-85de1d82f84a"), "Tenure", 0.0),
      Attribute(UUID.fromString("06525db5-1009-4dae-8107-e2bad553c459"), "Arrears", 1250.0),
      Attribute(UUID.fromString("7037b21c-2b62-4f09-b37d-ce022273b5e9"), "Satisfaction", 55.0)
    ),
    None,
    None
  )

  val customer3 = Customer(
    UUID.fromString("dd6242dd-44af-4160-aa7f-67513991e925"),
    "HighRoller",
    List(
      Attribute(UUID.fromString("266d5bec-3df1-4fcd-8c1c-7c3a01054865"), "Age", 45.0),
      Attribute(UUID.fromString("f32c1eda-ec5c-4f13-b25f-4fc9fa03d170"), "Income", 40000.0),
      Attribute(UUID.fromString("68bf9574-67b7-48f2-bef6-b81cf9ac4f90"), "Tenure", 0.0),
      Attribute(UUID.fromString("5cb4aa56-7d94-415f-a8ee-9bf4882a6f2c"), "Arrears", 2505.0),
      Attribute(UUID.fromString("3605e84b-08f6-4362-96ee-ea8fb983203e"), "Satisfaction", 50.0)
    ),
    None,
    None
  )

  val action1Effect1OnCustomer1 = Effect(
    UUID.fromString("ee27bb83-f822-441a-867b-990fc37d1ac2"),
    "ZeroArrears",
    EffectEnum.Effect,
    "Arrears",
    Some(-100),
    Some(0))

  val action1Effect2OnCustomer1 = Effect(
    UUID.fromString("77b3d54b-c952-4a3e-b354-8c110eee1ff1"),
    "Satisfy",
    EffectEnum.Effect,
    "Satisfaction",
    Some(25),
    Some(80))

  val action1Effect3OnCustomer1 = Effect(
    UUID.fromString("87ae0969-000e-42b0-b2e8-bf5dfa204cd5"),
    "Cooperative",
    EffectEnum.Affect,
    "Satisfy",
    Some(10),
    Some(100))

  val action1 = Action(
    UUID.fromString("806dd32c-3df2-48bf-8735-e5f9cfb33d3f"),
    "PayInFull",
    List(
      action1Effect1OnCustomer1,
      action1Effect2OnCustomer1,
      action1Effect3OnCustomer1
    ),
    Some(customer1)
  )

  val action2 = Action(
    UUID.fromString("f88aa899-24e7-4f03-857e-9c3016a46da7"),
    "Litigate",
    List(
      Effect(
        UUID.fromString("b55d07c7-c154-4e33-9237-6285bf83cdac"),
        "ZeroArrears",
        EffectEnum.Effect,
        "Arrears",
        Some(-100),
        Some(0)),
      Effect(
        UUID.fromString("410345f8-5507-4029-b656-ad8b14e00687"),
        "Dissatisfy",
        EffectEnum.Effect,
        "Satisfaction",
        Some(-25),
        Some(80))
    ),
    Some(customer1)
  )

  val action3 = Action(
    UUID.fromString("806dd32c-3df2-48bf-8735-e5f9cfb33d3f"),
    "PayInFull",
    List(
      Effect(
        UUID.fromString("ee27bb83-f822-441a-867b-990fc37d1ac2"),
        "ZeroArrears",
        EffectEnum.Effect,
        "Arrears",
        Some(-100),
        Some(0)),
      Effect(
        UUID.fromString("77b3d54b-c952-4a3e-b354-8c110eee1ff1"),
        "Satisfy",
        EffectEnum.Effect,
        "Satisfaction",
        Some(50),
        Some(20)),
      Effect(
        UUID.fromString("87ae0969-000e-42b0-b2e8-bf5dfa204cd5"),
        "Cooperative",
        EffectEnum.Affect,
        "Satisfy",
        Some(5),
        Some(90))
    ),
    Some(customer2)
  )

  val action4 = Action(
    UUID.fromString("f88aa899-24e7-4f03-857e-9c3016a46da7"),
    "Litigate",
    List(
      Effect(
        UUID.fromString("b55d07c7-c154-4e33-9237-6285bf83cdac"),
        "ZeroArrears",
        EffectEnum.Effect,
        "Arrears",
        Some(-100),
        Some(0)),
      Effect(
        UUID.fromString("410345f8-5507-4029-b656-ad8b14e00687"),
        "Dissatisfy",
        EffectEnum.Effect,
        "Satisfaction",
        Some(-90),
        Some(80))
    ),
    Some(customer2)
  )

  val action5 = Action(
    UUID.fromString("806dd32c-3df2-48bf-8735-e5f9cfb33d3f"),
    "PayInFull",
    List(
      Effect(
        UUID.fromString("ee27bb83-f822-441a-867b-990fc37d1ac2"),
        "ZeroArrears",
        EffectEnum.Effect,
        "Arrears",
        Some(-100),
        Some(0)),
      Effect(
        UUID.fromString("77b3d54b-c952-4a3e-b354-8c110eee1ff1"),
        "Satisfy",
        EffectEnum.Effect,
        "Satisfaction",
        Some(80),
        Some(7)),
      Effect(
        UUID.fromString("87ae0969-000e-42b0-b2e8-bf5dfa204cd5"),
        "Cooperative",
        EffectEnum.Affect,
        "Satisfy",
        Some(50),
        Some(50))
    ),
    Some(customer3)
  )

  val action6 = Action(
    UUID.fromString("f88aa899-24e7-4f03-857e-9c3016a46da7"),
    "Litigate",
    List(
      Effect(
        UUID.fromString("b55d07c7-c154-4e33-9237-6285bf83cdac"),
        "ZeroArrears",
        EffectEnum.Effect,
        "Arrears",
        Some(-100),
        Some(0)),
      Effect(
        UUID.fromString("410345f8-5507-4029-b656-ad8b14e00687"),
        "Dissatisfy",
        EffectEnum.Effect,
        "Satisfaction",
        Some(-25),
        Some(80))
    ),
    Some(customer3)
  )

  val trainingData = TrainingData(
    configuration.id,
    List(
      customer1,
      customer2,
      customer3
    ),
    List(
      action1,
      action2,
      action3,
      action4,
      action5,
      action6
    )
  )

  def getValidConfiguration() = { configuration }
  def getValidTrainingData() = { trainingData }

}
