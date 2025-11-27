# FLAS

Associated repository to the paper *FLAS: A combination of proactive and reactive auto-scaling architecture for distributed services* published in [Future Generation Computer Systems journal](https://doi.org/10.1016/j.future.2020.12.025) containing data, scripts and test code.

FLAS (Forecasted Load Auto-Scaling) is an auto-scaler for distributed services that combines the advantages of proactive and reactive approaches according to the situation to decide the optimal scaling actions in every moment. The main novelties introduced by FLAS are (i) a predictive model of the high-level metrics trend which allows to anticipate changes in the relevant SLA parameters (e.g. performance metrics such as response time or throughput) and (ii) a reactive contingency system based on the estimation of high-level metrics from resource use metrics, reducing the necessary instrumentation (less invasive) and allowing it to be adapted agnostically to different applications. We provide a FLAS implementation for the use case of a content-based publish–subscribe middleware (E-SilboPS) that is the cornerstone of an event-driven architecture. To the best of our knowledge, this is the first auto-scaling system for content-based publish–subscribe distributed systems (although it is generic enough to fit any distributed service). Through an evaluation based on several test cases recreating not only the expected contexts of use, but also the worst possible scenarios (following the Boundary-Value Analysis or BVA test methodology), we have validated our approach and demonstrated the effectiveness of our solution by ensuring compliance with performance requirements over 99% of the time.

## Reference

Rampérez, V., Soriano, J., Lizcano, D., & Lara, J. A. (2021). FLAS: A combination of proactive and reactive auto-scaling architecture for distributed services. Future Generation Computer Systems, 118, 56-72. doi: [10.1016/j.future.2020.12.025](https://doi.org/10.1016/j.future.2020.12.025).

```tex
@article{RAMPEREZ202156,
title = {FLAS: A combination of proactive and reactive auto-scaling architecture for distributed services},
journal = {Future Generation Computer Systems},
volume = {118},
pages = {56-72},
year = {2021},
issn = {0167-739X},
doi = {https://doi.org/10.1016/j.future.2020.12.025},
url = {https://www.sciencedirect.com/science/article/pii/S0167739X20330879},
author = {Víctor Rampérez and Javier Soriano and David Lizcano and Juan A. Lara},
keywords = {Cloud, Elasticity, Automatic scaling, Distributed systems}
}

```

